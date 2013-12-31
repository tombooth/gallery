(ns gallery.private
  [:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :as json]
            [gallery.data :as data]
            [gallery.s3 :as s3]])

  
(defn- process-request [request]
  (let [file (-> request :params :file)
        config-json (-> request :params :config)
        config (json/parse-string config-json true)]
    (if (= "pass-through" file)
      config
      (if (not (or (nil? file) (nil? config)))
        (let [url (s3/upload-file (:tempfile file))]
          (assoc config :url url :mime_type (:content-type file)))))))


(defroutes all-routes

  ;; user-id should be passed through as a query string if wanted
  
  (POST "/artwork" [user-id :as request]
        (if-let [artwork-json (process-request request)]
          (if-let [artwork (data/valid-artwork artwork-json)]
            (let [user (data/get-or-create-user user-id)]
              {:status 200
               :body (json/generate-string
                      (data/add-artwork user artwork))})
            {:status 400})
          {:status 400}))

  (POST "/artwork/:artwork-id/inspiration" [artwork-id :as request]
        (if-let [inspiration-json (process-request request)]
          (if-let [artwork (data/get-artwork artwork-id)]
            {:status 200
             :body (json/generate-string
                    (data/add-inspiration (:pid artwork) inspiration-json))}
            {:status 400})
          {:status 400}))
            
  (route/not-found "Not Found"))


(def handler (-> all-routes
                 ring.middleware.keyword-params/wrap-keyword-params
                 ring.middleware.multipart-params/wrap-multipart-params
                 ring.middleware.nested-params/wrap-nested-params
                 ring.middleware.params/wrap-params))

