(ns clak.bucket
  "This namespace is used for buckets operations."
  (:refer-clojure :exclude [keys])
  (:require [clj-http.client :as http]
            [clak.core :as core]
            [clak.json :as json]))

(defn buckets
  "Lists all buckets."
  []
  (-> (str core/*riak-url* "/buckets?buckets=true")
      http/get
      :body
      (json/json->clj)))

(defn keys
  "Lists all keys for the specified bucket."
  [bucket]
  (-> (str (core/bucket-url bucket) "/keys?keys=true")
      http/get
      :body
      (json/json->clj)))

(defn- bucket-props-url
  [bucket]
  (str (core/bucket-url bucket) "/props"))

(defn properties
  "Returns the properties of the specified bucket."
  [bucket]
  (-> (bucket-props-url bucket)
      http/get
      :body
      (json/json->clj)
      :props))

(defn update
  "Update properties of the specified bucket."
  [bucket props]
  (http/put (bucket-props-url bucket)
            {:content-type :json
             :accept :json
             :body (json/clj->json {"props" props})}))

(defn reset
  "Resets properties of the specified bucket to the default settings."
  [bucket]
  (-> (bucket-props-url bucket)
      http/delete))
