(ns gallery.test.public
  [:require [clojure.test :refer :all]
            [gallery.test.common :refer :all]
            [gallery.public :as public]
            [gallery.data :as data]])


(deftest public-integration-tests
  (testing "200/404"
    (db-test (let [artwork (data/add-artwork nil {:url "" :mime_type ""})]
               (is (= 200 (:status (make-request (str "/" (:pid artwork))
                                                 public/handler))))
               (is (= 404 (:status (make-request "/7n" public/handler)))))))
  
  (db-testing "number of inspiration matches"
              (let [artwork (data/add-artwork nil {:url "" :mime_type ""})
                    artwork-pid (:pid artwork)
                    inspiration-1 (data/add-inspiration artwork-pid {:url "1" :mime_type ""})
                    i-1-pid (:pid inspiration-1)
                    inspiration-2 (data/add-inspiration artwork-pid {:url "2" :mime_type ""})
                    i-2-pid (:pid inspiration-2)
                    response (make-request (str "/" artwork-pid) public/handler)]
                (is (re-matches (re-pattern (str ".*<ul class=\"inspiration\">.*"
                                                 "<a href=\"/" artwork-pid "/inspiration/" i-1-pid "\">.*"
                                                 "<a href=\"/" artwork-pid "/inspiration/" i-2-pid "\">.*")) (:body response)))))
  
  (db-testing "inspiration not there if none"
              (let [artwork (data/add-artwork nil {:url "" :mime_type ""})
                    response (make-request (str "/" (:pid artwork)) public/handler)]
                (is (nil? (re-matches #".*<ul class=\"inspiration\">.*"
                                      (:body response))))))

  (db-testing "inspiration redirect"
              (let [inspiration-url "http://google.co.uk"
                    artwork (data/add-artwork nil {:url "" :mime_type ""})
                    artwork-pid (:pid artwork)
                    inspiration (data/add-inspiration artwork-pid
                                                      {:url inspiration-url :mime_type ""})
                    inspiration-pid (:pid inspiration)
                    response (make-request (str "/" artwork-pid
                                                "/inspiration/" inspiration-pid)
                                           public/handler)]
                (is (= 303 (:status response)))
                (is (= inspiration-url ((:headers response) "Location")))))

  (db-testing "inspiration fail"
              (let [response (make-request "/5r/inspiration/5r" public/handler)]
                (is (= 404 (:status response)))))

  (db-testing "description not included"
              (let [artwork (data/add-artwork nil {:url "" :mime_type ""})
                    response (make-request (str "/" (:pid artwork)) public/handler)]
                (is (= 200 (:status response)))
                (is (nil? (re-matches #".*<p class=\"description\">.*"
                                      (:body response))))))

  (db-testing "description included"
              (let [artwork (data/add-artwork nil {:url "" :mime_type ""
                                                   :description "asdfasdf"})
                    response (make-request (str "/" (:pid artwork)) public/handler)]
                (is (= 200 (:status response)))
                (is (re-matches #".*asdfasdf.*" (:body response))))))

