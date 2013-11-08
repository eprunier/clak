(ns clak.object
  "This namespace is used for key/value objects operations."
  (:require [clojure.string :as str]
            [clj-http.client :as http]
            [clak.core :as core]
            [clak.json :as json]))

(defn- result-postprocessing
  "Post-process query result."
  [{status :status headers :headers data :body :as result}]
  (if (= 404 status)
    {:headers headers 
     :body nil}
    (let [content-type (headers "content-type")]
      (if (= "application/clojure" content-type)
        (assoc result :body (json/json->clj data))
        result))))

(defn fetch
  "Reads an object from the specified bucket and key"
  [bucket key]
  (-> (core/key-url bucket key)
      (http/get {:throw-exceptions false})
      result-postprocessing
      :body))

(defn- map->headers
  "Parse a clojure map and generate the related headers map"
  [prefix metadata]
  (reduce #(assoc %
             (str (name prefix) (name (key %2)))
             (val %2))
          {}
          metadata))

(defn- ->headers
  [{:keys [data as links indexes metadata]
    :or {as "application/clojure"}
    :as content}]
  (let [headers {"Content-Type" as
                 "Link" links}
        index-metadata (map->headers "x-riak-index-" indexes)
        other-metadata (map->headers "x-riak-meta-" metadata)]
    (merge headers index-metadata other-metadata)))

(defn- data-preprocessing
  "Pre-process data according to content-type defined in headers."
  [headers data]
  (let [content-type (headers "Content-Type")]
    (if (= "application/clojure" content-type)
      (json/clj->json data)
      data)))

(defn- send-store-request
  "Send a HTTP store request based on the specified method (:put or :post)."
  [method url data]
  (condp = method
    :put (http/put url data)
    :post (http/post url data)))

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
     (store bucket nil content))
  ([bucket key {:keys [data] :as content}]
     (let [headers (->headers content)
           request-data {:headers headers
                         :body (data-preprocessing headers data)
                         :decompress-body false}]
       (if (nil? key)
         (send-store-request :post
                             (str (core/bucket-url bucket) "/keys")
                             request-data)
         (send-store-request :put
                             (core/key-url bucket key)
                             request-data)))))

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
