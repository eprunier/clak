(ns clak.server
  "This namespace is used for server operations.

   Start with a connection :
     (clak.server/connect! \"http://localhost\" \"8098\")

   Then you can issue some commands :
     (clak.server/ping)
     (clak.bucket/buckets)"
  (:require [clj-http.client :as http]
            [clak.core :as core]
            [clak.json :as json]))

(defn connect!
  "Define the Riak URL and port used by each command."
  ([]
     (connect! "http://localhost"))
  ([url]
     (connect! url 8098))
  ([url port]
     (alter-var-root (var core/*riak-url*)
                     (constantly (str url ":" port)))))

(defn ping
  "Checks if the server is alive (should return \"OK\")."
  ([]
     (-> (str core/*riak-url* "/ping")
         http/get
         :body))
  ([url port]
     (-> (str url ":" port "/ping")
         http/get
         :body)))

(defn stats
  "Returns informations of the requested node as a clojure map."
  []
  (-> (str core/*riak-url* "/stats")
      http/get
      :body
      (json/json->clj)))

(defn ring-members
  "Returns nodes in the current ring."
  []
  (:ring_members (stats)))

(defn ring-ownership
  "Return the ring ownership."
  []
  (let [pattern #"\{'([\w\.@]*)',([\d]+)\}"
        ownership (->> (stats)
                       :ring_ownership)]
    (reduce #(assoc % (second %2) (Integer/valueOf (last %2)))
            {}
            (re-seq pattern ownership))))
