(ns gallery.test.common
  [:require [korma.db :as db]
            [korma.core :as korma]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [gallery.schema :as schema]])


(def db-spec (db/sqlite3 {:db "test/test.db"}))

(defmacro db-test [& body]
  `(do
     (schema/exec db-spec schema/create-all)
     (try (db/with-db db-spec ~@body)
          (finally (schema/exec db-spec schema/drop-all)))))

(defn sql-stamp [year month day hour minute]
  (coerce/to-timestamp
    (time/date-time year month day hour minute 0 0)))


