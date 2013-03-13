(ns clak.query
  (:require [clojure.string :as str]
            [clj-http.client :as http]
            [clak.core :as core]
            [clak.json :as json]))

(defn link
  "Define a link for the link walking."
  [bucket tag keep]
  (str/join "," [(name bucket) (name tag) keep]))

(defn walk
  "Link walking.

   Example :
   (walk :origin-bucket :origin-key
      (link :bucket1 :tag1 true)
      (link :bucket2 :tag2 true))"
  [bucket key & links]
  (let [key-url (core/key-url bucket key)]
    (-> (str key-url "/" (str/join "/" links))
        http/get
        :body)))