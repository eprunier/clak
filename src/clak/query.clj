(ns clak.query
  (:require [clojure.string :as str]
            [clojure.walk :as w]
            [clj-http.client :as http]
            [clak.core :as core]
            [clak.json :as json]))

(defn- trim-seq
  "Trim all strings in the data seq."
  [data]
  (map str/trim data))

(defn- clean
  "Remove empty strings and \"--\"."
  [parts]
  (filter (fn [part]
            (and (not= "" part)
                 (not= "--" part)
                 (not (re-find #"^Content-Type: multipart/mixed; boundary=[\w]*$" part))))
          (trim-seq parts)))

(defn- split-steps
  "Parses multipart data and returns a sequence of strings representing steps."
  [multipart]
  (let [boundary (re-find #"^--[a-zA-Z0-9]*" multipart)]
    (-> multipart
        (str/split (re-pattern boundary))
        clean)))

(defn- split-results
  "Returns a sequence of strings representing the results for the specified step."
  [step]
  (let [matches (re-find #"^Content-Type: multipart/mixed; boundary=([\w]*)" step)
        boundary (second matches)]
    (->> (str "--" boundary)
         re-pattern
         (str/split step)
         clean)))

(defn- parse-headers
  "Takes a string representing headers and returns a map with keywordized keys."
  [headers]
  (let [lines (-> headers
                  str/split-lines)]
    (->> (map #(apply hash-map (str/split % #": ")) lines)
         (apply merge)
         w/keywordize-keys)))

(defn parse-result
  "Takes a string representing a result and returns a map containing :header and :body."
  [result]
  (let [result-items (-> result
                         (str/split #"\n\n")
                         clean)
        headers (parse-headers (first result-items))
        body (second result-items)]
    {:header headers
     :body body}))

(defn parse-step
  "Parses results for the specified step and returns a vector of maps representing results."
  [step]
  (let [results (split-results step)]
    (->> results
        (map parse-result)
        vector)))

(defn parse-multipart
  "Parses multipart walk result and returns a vector of steps."
  [multipart]
  (let [steps (split-steps multipart)]
    (reduce (fn [results step]
              (apply conj
                     results
                     (parse-step step)))
            []
            steps)))

(defn step
  "Defines a step for the link walking."
  [bucket tag keep]
  (str/join "," [(name bucket) (name tag) keep]))

(defn walk
  "Link walking.

   Example :
   (walk :origin-bucket :origin-key
      (step :bucket1 :tag1 true)
      (step :bucket2 :tag2 true))"
  [bucket key & links]
  (let [key-url (core/key-url bucket key)
        multipart (-> (str key-url "/" (str/join "/" links))
                      http/get
                      :body)]
    (parse-multipart multipart)))
