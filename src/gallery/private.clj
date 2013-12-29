(ns gallery.private
  [:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [gallery.data :as data]])


(defroutes all-routes

  (POST "/:user-id" [user-id :as request]
        (let [artwork-json (json/parse-string (slurp (:body request)) true)]
          (if-let [artwork (data/valid-artwork artwork-json)]
            (let [user (data/get-or-create-user user-id)]
              {:status 200
               :body (json/generate-string
                      (data/add-artwork user artwork))})
            {:status 400})))

  (POST "/:user-id/:artwork-id" [user-id artwork-id :as request]
        (let [inspiration-json (json/parse-string (slurp (:body request)) true)
              user (data/get-user user-id)
              artwork (data/get-artwork artwork-id)]
          (if (and user artwork)
            {:status 200
             :body (json/generate-string
                    (data/add-inspiration (:pid artwork) inspiration-json))}
            {:status 400})))
            
  (route/not-found "Not Found"))


(def handler (-> all-routes
                 ring.middleware.multipart-params/wrap-multipart-params
                 ring.middleware.keyword-params/wrap-keyword-params
                 ring.middleware.nested-params/wrap-nested-params
                 ring.middleware.params/wrap-params))

