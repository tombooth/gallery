(ns gallery.test.data
  [:require [clojure.test :refer :all]
            [gallery.test.common :refer :all]
            [gallery.data :as data]])

(def user-id (apply str (repeat 64 "4")))

(def artwork {:url "url" :inspiration_url "inspiration"})



(deftest user-integration-test
  (testing "create and get"
    (db-test (data/create-user user-id)
             (is (= user-id (:id (data/get-user user-id)))))))



(deftest artwork-unit-tests
  (testing "validating an artwork"
    (is (nil? (data/valid-artwork {})))
    (is (nil? (data/valid-artwork {:url "foo"})))
    (is (nil? (data/valid-artwork {:inspiration_url "foo"})))
    (let [artwork {:url "foo" :inspiration_url "foo" :id 1}
          validated-artwork (data/valid-artwork artwork)]
      (is (and (= "foo" (:url validated-artwork))
               (= "foo" (:inspiration_url validated-artwork))
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
                       {:url "1" :inspiration_url "" :created (sql-stamp 2013 12 25 12 3)})
     (data/add-artwork nil
                       {:url "2" :inspiration_url "" :created (sql-stamp 2013 12 25 12 0)})
     (data/add-artwork nil
                       {:url "3" :inspiration_url "" :created (sql-stamp 2013 12 25 12 4)})
     (data/add-artwork nil
                       {:url "4" :inspiration_url "" :created (sql-stamp 2013 12 25 12 2)})
     (data/add-artwork nil
                       {:url "5" :inspiration_url "" :created (sql-stamp 2013 12 25 12 1)})
     (data/add-artwork nil
                       {:url "6" :inspiration_url "" :created (sql-stamp 2013 12 25 11 0)})
     (let [most-recent (data/get-recent-artworks 5)]
       (is (= 5 (count most-recent)))
       (is (= "3" (-> most-recent first :url)))
       (is (= "2" (-> most-recent last :url)))))))


