(ns clak.core
  (:refer-clojure :exclude [keys])
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

(declare ^{:private true :dynamic true} riak-url)

;;; JSON operations
(defn json->clj [json-data]
  (json/parse-string json-data true))

(defn clj->json [object]
  (json/generate-string object))


;;; Connection operations
(defn connect!
  "Define the Riak URL"
  [url]
  (alter-var-root (var riak-url)
                  (constantly url)))

(defn ping
  "Checks if the server is alive (should returns OK)"
  []
  (-> (str riak-url "/ping")
      http/get
      :body))

(defn stats
  "Reports informations of the requested node"
  []
  (-> (str riak-url "/stats")
      http/get
      :body
      json->clj))


;;; Bucket operations
(defn- bucket-url
  [bucket]
  (str riak-url "/buckets/" bucket))

(defn buckets
  "Lists all buckets"
  []
  (-> (str riak-url "/buckets?buckets=true")
      http/get
      :body
      json->clj))

(defn keys
  "Lists all keys for the specified bucket"
  [bucket]
  (-> (str (bucket-url bucket) "/keys?keys=true")
      http/get
      :body
      json->clj))


;;; Object/Key operations
(defn fetch
  "Reads an object from the specified bucket and key"
  [bucket key]
  (-> (str (bucket-url bucket) "/keys/" key)
      http/get))

(defn- parse-metadata
  "Parse metadata and generate related headers map"
  [metadata]
  (reduce #(assoc %
             (str "X-Riak-Meta-" (name (key %2)))
             (val %2))
          {}
          metadata))

(defn- ->headers
  "Available option keys :
     :content-type
     :link
     :indexes
     :metadata"
  [{:keys [content-type link indexes metadata]
    :or {content-type "application/octet-stream"
         link ""
         indexes ""}
    :as options}]
  (let [headers {"Content-Type" content-type
                 "Link" link
                 "Indexes" indexes}]
    (if metadata
      (merge headers (parse-metadata metadata))
      headers)))

(defn store-without-key
  "Store data under the specified bucket and returns a generated key."
  [bucket data & [options]]
  (let [headers (->headers options)]
    (http/post (str (bucket-url bucket) "/keys")
               {:headers headers
                :body data
                :decompress-body false})))

(defn store
  "Store data under the specified bucket and key"
  [bucket key data & [options]]
  (let [headers (->headers options)]
    (http/put (str (bucket-url bucket) "/keys/" key)
               {:headers headers
                :body data
                :decompress-body false})))

(defn delete
  "Delete an object from the specified bucket and key"
  [bucket key]
  (http/delete (str (bucket-url bucket) "/keys/" key)))


;;; Search operations


;;; Map/reduce operations
