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
     (let [response (make-request :post "/artwork"
                                  private/handler
                                  {:user-id user-id}
                                  "{\"url\":\"asdf\",\"mime_type\":\"mime\"}")]
       (is (= 200 (:status response)))
       (is (not (nil? (data/get-user user-id))))
       (let [artworks (korma/select data/artworks)]
         (is (= 1 (count artworks)))
         (is (= "asdf" (-> artworks first :url)))
         (is (= user-id (-> artworks first :user_id)))))))

  (db-testing "test add inspiration end point"
              (let [artwork (data/add-artwork nil {:url "" :mime_type ""})
                    artwork-pid (:pid artwork)
                    response (make-request :post (str "/artwork/" artwork-pid "/inspiration")
                                           private/handler {}
                                           "{\"url\":\"a\",\"mime_type\":\"b\"}")]
                (is (= 200 (:status response)))
                (let [inspiration (korma/select data/inspiration)]
                  (is (= 1 (count inspiration)))
                  (is (= "a" (-> inspiration first :url)))
                  (is (= "b" (-> inspiration first :mime_type))))))

  (db-testing "test description through api"
     (let [response (make-request :post "/artwork"
                                  private/handler
                                  {:user-id user-id}
                                  "{\"url\":\"asdf\",\"mime_type\":\"mime\",\"description\":\"foo\"}")]
       (is (= 200 (:status response)))
       (let [artworks (korma/select data/artworks)]
         (is (= 1 (count artworks)))
         (is (= "foo" (-> artworks first :description))))))

  (db-testing "test config through api"
     (let [response (make-request :post "/artwork"
                                  private/handler
                                  {:user-id user-id}
                                  "{\"url\":\"asdf\",\"mime_type\":\"mime\",\"config\":\"foo\"}")]
       (is (= 200 (:status response)))
       (let [artworks (korma/select data/artworks)]
         (is (= 1 (count artworks)))
         (is (= "foo" (-> artworks first :config)))))))

