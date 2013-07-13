(defproject clak "0.1.0-SNAPSHOT"
  :description "Simple Riak client for Clojure based on new HTTP API format."
  :url "https://github.com/eprunier/clak"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.6.5"]
                 [cheshire "5.0.2"]]
  :codox {:exclude clak.core})
