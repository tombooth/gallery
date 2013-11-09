(defproject gallery "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring-serve "0.1.2"]
                 [docopt "0.6.1"]
                 [korma "0.3.0-RC5"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [cheshire "5.2.0"]
                 [hashobject/hashids "0.2.0"]
                 [clj-time "0.6.0"]]
  :plugins [[lein-ring "0.8.7"]]
  :main gallery.entry
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
