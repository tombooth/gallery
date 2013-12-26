(ns gallery.test.public
  [:require [clojure.test :refer :all]
            [gallery.test.common :refer :all]
            [gallery.public :as public]
            [gallery.data :as data]])


(deftest public-integration-tests
  (testing "200/404"
    (db-test (let [artwork (data/add-artwork nil {:url "" :inspiration_url ""})]
               (is (= 200 (:status (make-request (str "/" (:pid artwork))
                                                 public/all-routes))))
               (is (= 404 (:status (make-request "/7n" public/all-routes))))))))

