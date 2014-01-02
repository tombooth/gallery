(defproject gallery "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring-server "0.3.1"]
                 [hiccup "1.0.4"]
                 [docopt "0.6.1"]
                 [korma "0.3.0-RC5"]
                 [lobos "1.0.0-beta1"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [com.h2database/h2 "1.3.170"]
                 [cheshire "5.2.0"]
                 [hashobject/hashids "0.2.0"]
                 [clj-time "0.6.0"]
                 [digest "1.4.3"]
                 [clj-aws-s3 "0.3.7"]]
  :plugins [[lein-ring "0.8.7"]]
  :main gallery.entry
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
