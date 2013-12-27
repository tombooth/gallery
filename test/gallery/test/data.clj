(ns gallery.test.data
  [:require [clojure.test :refer :all]
            [korma.core :as korma]
            [hashids.core :as hashids]
            [gallery.test.common :refer :all]
            [gallery.data :as data]])

(def user-id (apply str (repeat 64 "4")))

(def artwork {:url "url"})



(deftest user-integration-test
  (testing "create and get"
    (db-test (data/create-user user-id)
             (is (= user-id (:id (data/get-user user-id)))))))



(deftest artwork-unit-tests
  (testing "validating an artwork"
    (is (nil? (data/valid-artwork {})))
    (let [artwork {:url "foo" :id 1}
          validated-artwork (data/valid-artwork artwork)]
      (is (and (= "foo" (:url validated-artwork))
               (nil? (:id validated-artwork)))))))




(deftest artwork-integration-tests
  (testing "artwork gets extra fields"
    (db-test
     (data/create-user user-id)
     (let [saved-artwork (data/add-artwork {:id user-id} artwork)]
       (is (= user-id (:user_id saved-artwork)))
       (is (not (nil? (:pid saved-artwork)))))))

  (testing "recent 5 makes sense"
    (db-test
     (data/add-artwork nil
                       {:url "1" :created (sql-stamp 2013 12 25 12 3)})
     (data/add-artwork nil
                       {:url "2" :created (sql-stamp 2013 12 25 12 0)})
     (data/add-artwork nil
                       {:url "3" :created (sql-stamp 2013 12 25 12 4)})
     (data/add-artwork nil
                       {:url "4" :created (sql-stamp 2013 12 25 12 2)})
     (data/add-artwork nil
                       {:url "5" :created (sql-stamp 2013 12 25 12 1)})
     (data/add-artwork nil
                       {:url "6" :created (sql-stamp 2013 12 25 11 0)})
     (let [most-recent (data/get-recent-artworks 5)]
       (is (= 5 (count most-recent)))
       (is (= "3" (-> most-recent first :url)))
       (is (= "2" (-> most-recent last :url)))
       (is (not (nil? (-> most-recent first :inspiration)))))))

  (db-testing "artwork with no inspiration when retrieved"
              (data/create-user user-id)
              (let [saved-artwork (data/add-artwork {:id user-id} artwork)
                    retrieved-artwork (data/get-artwork (:pid saved-artwork))]
                (is (= [] (:inspiration retrieved-artwork)))))
  
  (db-testing "artwork with inspiration"
              (data/create-user user-id)
              (let [saved-artwork (data/add-artwork {:id user-id} artwork)
                    artwork-pid (:pid saved-artwork)]
                (data/add-inspiration artwork-pid {:url "" :mime_type ""})
                (data/add-inspiration artwork-pid {:url "" :mime_type ""})
                (let [retrieved-artwork (data/get-artwork artwork-pid)]
                  (is (= 2 (count (:inspiration retrieved-artwork))))
                  (is (every? #(-> % :pid nil? not) (:inspiration retrieved-artwork)))))))



(deftest inspiration-unit-tests
  (testing "verify inspiration"
    (is (nil? (data/verify-inspiration {})))
    (is (nil? (data/verify-inspiration {:url "a"})))
    (is (nil? (data/verify-inspiration {:mime_type "b"})))
    (let [verified (data/verify-inspiration {:url "a" :mime_type "b" :id 1})]
      (is (nil? (:id verified)))
      (is (= "a" (:url verified)))
      (is (= "b" (:mime_type verified))))))



(deftest inspiration-integration-tests
  (testing "requires artwork-id"
    (is (nil? (data/add-inspiration nil {}))))
  
  (db-testing "add some inspiration"
    (data/create-user user-id)
    (let [artwork (data/add-artwork {:id user-id} {:url ""})
          artwork-pid (:pid artwork)
          artwork-id (hashids/decrypt artwork-pid data/hashids-salt)
          saved-inspiration (data/add-inspiration (:pid artwork) {:url "" :mime_type ""})]
      (is (= 1 (count (korma/select data/inspiration
                                    (korma/where {:artworks_id artwork-id})))))
      (is (nil? (:id saved-inspiration)))
      (is (not (nil? (:pid saved-inspiration))))
      (is (= "" (:url saved-inspiration)))
      (is (= "" (:mime_type saved-inspiration))))))



