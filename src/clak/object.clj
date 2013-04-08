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

(defn- map->headers
  "Parse a clojure map and generate the related headers map"
  [prefix metadata]
  (reduce #(assoc %
             (str prefix (name (key %2)))
             (val %2))
          {}
          metadata))

(defn- ->headers
  [{:keys [data as links indexes metadata]
    :or {as "application/json"}
    :as content}]
  (let [headers {"Content-Type" as
                 "Link" links}
        index-metadata (map->headers "X-Riak-Index-" indexes)
        other-metadata (map->headers "X-Riak-Meta-" metadata)]
    (merge headers index-metadata other-metadata)))

(defn store
  "Store data under the specified bucket and the given key
   (or a generated one if not provided).

   content keys :
     - :data
     - :as       (Content-Type)
     - :links    (produced by the link-to function)
     - :indexes  (key/value map)
     - :metadata (key/value map)"
  ([bucket {:keys [data] :as content}]
     (let [headers (->headers content)]
       (http/post (str (core/bucket-url bucket) "/keys")
                  {:headers headers
                   :body data
                   :decompress-body false})))
  ([bucket key {:keys [data] :as content}]
     (let [headers (->headers content)]
       (http/put (core/key-url bucket key)
                 {:headers headers
                  :body data
                  :decompress-body false}))))

(defn delete
  "Delete an object from the specified bucket and key"
  [bucket key]
  (http/delete (core/key-url bucket key)))

(defn- create-link
  "Create the string that corresponds to a link."
  [bucket key tag]
  (str "</buckets/" (name bucket) "/keys/" (name key) ">; riaktag=\"" (name tag) "\""))

(defn link-to
  "Create a string with multiple links.
   This string have to be included in the :link option
   when storing the associated object.

   Example :
   (link-to
      [:bucket1 :key1 :tag1]
      [:bucket2 :key2 :tag2]"
  [& links]
  (str/join ", " (map #(apply create-link %) links)))
