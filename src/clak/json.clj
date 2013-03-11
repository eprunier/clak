(ns clak.json
  "This namespace contains JSON utilities"
  (:require [cheshire.core :as json]))

(defn json->clj
  "Convert JSON to Clojure data."
  [json-data]
  (json/parse-string json-data true))

(defn clj->json
  "Convert Clojure data to JSON."
  [object]
  (json/generate-string object))
