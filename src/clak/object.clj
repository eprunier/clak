(ns clak.object
  "This namespace is used for key/value objects operations."
  (:require [clojure.string :as str]
            [clj-http.client :as http]
            [clak.core :as core]
            [clak.json :as json]))

(defn fetch
  "Reads an object from the specified bucket and key"
  [bucket key]
  (-> (core/key-url bucket key)
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
  [{:keys [content-type links indexes metadata]
    :or {content-type "application/octet-stream"
         links ""
         indexes ""}
    :as options}]
  (let [headers {"Content-Type" content-type
                 "Link" links
                 "Indexes" indexes}]
    (if metadata
      (merge headers (parse-metadata metadata))
      headers)))

(defn store-without-key
  "Store data under the specified bucket and returns a generated key."
  [bucket data & [options]]
  (let [headers (->headers options)]
    (http/post (str (core/bucket-url bucket) "/keys")
               {:headers headers
                :body data
                :decompress-body false})))

(defn store
  "Store data under the specified bucket and key.

   Available options :
     - :content-type
     - :links
     - :indexes"
  [bucket key data & [options]]
  (let [headers (->headers options)]
    (println headers)
    (http/put (core/key-url bucket key)
              {:headers headers
               :body data
               :decompress-body false})))

(defn delete
  "Delete an object from the specified bucket and key"
  [bucket key]
  (http/delete (core/key-url bucket key)))

(defn link
  "Create the string that corresponds to a link."
  [bucket key tag]
  (str "</buckets/" (name bucket) "/keys/" (name key) ">; riaktag=\"" (name tag) "\""))

(defn create-links
  "Create a string with multiple links.
   This string have to be included in the :link option
   when storing the associated object.

   Example :
   (create-links
      (link :bucket1 :key1 :tag1)
      (link :bucket2 :key2 :tag2)"
  [& links]
  (str/join ", " links))
