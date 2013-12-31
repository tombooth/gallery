(ns gallery.entry
  (:gen-class)
  [:require [gallery.public :as public]
            [gallery.private :as private]
            [gallery.data :as data]
            [gallery.schema :as schema]
            [gallery.s3 :as s3]
            [ring.server.standalone :refer [serve]]
            [docopt.core :as dc]
            [docopt.match :as dm]
            [korma.db :as db]])


(defn- load-db [arg-map]
  (if-let [h2-path (arg-map "--h2")]
    (db/h2 {:db h2-path})
    (db/postgres {:db (arg-map "--db")
                  :user (arg-map "--db-user")
                  :password (arg-map "--db-password")
                  :host (arg-map "--db-server")})))

(defn- start-web [arg-map handler]
  (let [port (Integer/parseInt (arg-map "--port"))]
    (data/setup-db (load-db arg-map))
    (s3/setup {:access-key  (arg-map "--s3-access-key")
               :secret-key  (arg-map "--s3-secret-key")
               :endpoint    (arg-map "--s3-endpoint")
               :bucket-name (arg-map "--s3-bucket")
               :base-url    (arg-map "--s3-base-url")})
    (serve handler {:port port :open-browser? false})))

(defn- exec-schema [arg-map]
  (let [db-spec (load-db arg-map)
        schema-fn (if (arg-map "--drop")
                    schema/drop-all
                    schema/create-all)]
    (schema/exec db-spec schema-fn)))


(def usage-string "Gallery

Usage:
  gallery public [--port=<num>] (--h2=<path>|--db=<db> --db-server=<server> --db-user=<user> --db-password=<password>)
  gallery private [--port=<num>] (--h2=<path>|--db=<db> --db-server=<server> --db-user=<user> --db-password=<password>) --s3-bucket=<name> --s3-access-key=<key> --s3-secret-key=<key> --s3-endpoint=<url> --s3-base-url=<url>
  gallery schema [--drop] (--h2=<path>|--db=<db> --db-server=<server> --db-user=<user> --db-password=<password>)
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
  (let [arg-map (dm/match-argv (dc/parse usage-string) args)]
    (cond 
      (or (nil? arg-map)
          (arg-map "--help")) (println usage-string)
          (arg-map "--version") (println version)
          (arg-map "public") (start-web arg-map public/handler)
          (arg-map "private") (start-web arg-map private/handler)
          (arg-map "schema") (exec-schema arg-map))))

