(ns gallery.test.public
  [:require [clojure.test :refer :all]
            [gallery.test.common :refer :all]
            [gallery.public :as public]
            [gallery.data :as data]])


(deftest public-integration-tests
  (testing "200/404"
    (db-test (let [artwork (data/add-artwork nil {:url ""})]
               (is (= 200 (:status (make-request (str "/" (:pid artwork))
                                                 public/all-routes))))
               (is (= 404 (:status (make-request "/7n" public/all-routes)))))))
  
  (db-testing "number of inspiration matches"
              (let [artwork (data/add-artwork nil {:url ""})
                    artwork-pid (:pid artwork)
                    inspiration-1 (data/add-inspiration artwork-pid {:url "1" :mime_type ""})
                    inspiration-2 (data/add-inspiration artwork-pid {:url "2" :mime_type ""})
                    response (make-request (str "/" artwork-pid) public/all-routes)]
                (is (re-matches #".*<ul class=\"inspiration\">.*<a href=\"1\">.*<a href=\"2\">.*" (:body response)))))
  
  (db-testing "inspiration not there if none"
              (let [artwork (data/add-artwork nil {:url ""})
                    response (make-request (str "/" (:pid artwork)) public/all-routes)]
                (is (nil? (re-matches #".*<ul class=\"inspiration\">.*"
                                      (:body response)))))))

