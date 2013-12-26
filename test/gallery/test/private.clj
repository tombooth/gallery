(ns gallery.test.private
  [:require [clojure.test :refer :all]
   [korma.core :as korma]
            [gallery.test.common :refer :all]
            [gallery.private :as private]
   [gallery.data :as data]])

(def user-id (apply str (repeat 64 "3")))

(deftest private-integration-tests
  (testing "test end to end"
    (db-test
     (let [response (make-request :post (str "/" user-id)
                                  private/all-routes
                                  {:user-id user-id}
                                  "{\"url\":\"asdf\",\"inspiration_url\":\"\"}")]
       (is (= 200 (:status response)))
       (is (not (nil? (data/get-user user-id))))
       (let [artworks (korma/select data/artworks)]
         (is (= 1 (count artworks)))
         (is (= "asdf" (-> artworks first :url))))))))

