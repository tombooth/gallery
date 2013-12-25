(ns gallery.test.data
  [:require [clojure.test :refer :all]
            [korma.db :as db]
            [gallery.schema :as schema]
            [gallery.data :as data]])

(def test-db-spec (db/sqlite3 {:db "test/test.db"}))

(def user-id (apply str (repeat 64 "4")))

(defmacro db-test [& body]
  `(do
     (schema/exec test-db-spec schema/create-all)
     (db/with-db test-db-spec ~@body)
     (schema/exec test-db-spec schema/drop-all)))

(deftest user-integration-test
  (testing "create and get"
    (db-test (data/create-user user-id)
             (is (= user-id (:id (data/get-user user-id)))))))

