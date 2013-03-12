(ns clak.core)

(declare ^:dynamic *riak-url*)

(defn bucket-url
  [bucket]
  (str *riak-url* "/buckets/" (name bucket)))

(defn key-url
  [bucket key]
  (str (bucket-url (name bucket)) "/keys/" (name key)))
