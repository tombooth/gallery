(ns gallery.entry
  (:gen-class)
  [:require [gallery.public :as public]
            [gallery.private :as private]
            [gallery.data :as data]
            [ring.server.standalone :refer [serve]]
            [docopt.core :as dc]
            [docopt.match :as dm]])

(def usage-string "Gallery

Usage:
  gallery (public|private) [--port=<num>] --db=<db> --db-server=<server> --db-user=<user> --db-password=<password>
  gallery -h | --help
  gallery -v | --version

Options:
  -h --help                 Show this screen.
  -v --version              Show version.
  --port=<num>              Port to start the web server on [default:8080].
  --db=<db>                 Database to connect to [default:gallery].
  --db-server=<server>      Name of the server hosting PostgreSQL [default:localhost].
  --db-user=<user>          Username to use to connect [default:postgres].
  --db-password=<password>  Password used to connect.")

(def version "Gallery 0.1.0")

(defn -main [& args]
  (let [arg-map (dm/match-argv (dc/parse usage-string) args)
        port (Integer/parseInt (arg-map "--port"))
        db (data/setup-db (arg-map "--db")
                          (arg-map "--db-server")
                          (arg-map "--db-user")
                          (arg-map "--db-password"))
        server-args { :port port :open-browser? false }]
    (cond 
      (or (nil? arg-map)
          (arg-map "--help")) (println usage-string)
          (arg-map "--version") (println version)
          (arg-map "public") (serve public/all-routes server-args)
          (arg-map "private") (serve private/all-routes server-args))))

