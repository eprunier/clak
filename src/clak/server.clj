(ns clak.server
  "This namespace is used for server operations."
  (:require [clj-http.client :as http]
            [clak.core :as core]
            [clak.json :as json]))

(defn connect!
  "Define the Riak URL"
  [url]
  (alter-var-root (var core/*riak-url*)
                  (constantly url)))

(defn ping
  "Checks if the server is alive (should returns OK)"
  []
  (-> (str core/*riak-url* "/ping")
      http/get
      :body))

(defn stats
  "Reports informations of the requested node"
  []
  (-> (str core/*riak-url* "/stats")
      http/get
      :body
      (json/json->clj)))


