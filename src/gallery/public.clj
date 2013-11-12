(ns gallery.public
  [:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [hiccup.util :refer [url-encode]]
            [clj-time.coerce :as time-coerce]
            [clj-time.format :as time-format]
            [gallery.data :as data]])

(defn in-frame [title & rest]
  (html
   "<!DOCTYPE html>"
   [:html
    [:head
     [:title "pollock.artcollective.io"]
     [:link {:rel "stylesheet" :href "/static/ionicons/ionicons.css"}]
     [:link {:rel "stylesheet" :href "/static/main.css"}]]
    [:body
     rest]]))

(defn gen-artwork [artwork]
  [:div {:class "exhibit"}
    [:div {:class "notes"}
     [:h1 "Pollock"]
     [:h2 (str "Experimental #" (:pid artwork))]
     [:p "Some blurb about how this piece of work was made"]
     [:p {:class "method"} "digital canvas, binary paint"]
     [:ul {:class "inspiration"}
      [:li
       [:a {:href (:inspiration_url artwork)}
        [:i {:class "ion-play"}]
        "Listen to the inspiration."]]]
     [:ul {:class "sharing"}
      [:li
       [:a {:href (str "//twitter.com/share?url="
                       (url-encode (str "http://pollock.artcollective.io/"
                                        (:pid artwork)))
                       "&related=tombooth") }
        [:i {:class "ion-social-twitter"}]
        "Share with your friends."]]]]
     [:div {:class "artwork"} [:img {:src (:url artwork)}]]])

(defn gen-404 [pid]
  (in-frame pid
            [:h2 "Artwork not found"]))


(defroutes all-routes
  (GET "/" []
       (in-frame ""
                 [:p {:class "page-middle"} "Call me and leave a message on my number below."]
                 [:p {:class "page-middle"} "+441290211866"]))
  (GET "/:pid" [pid]
       (if-let [artwork (data/get-artwork pid)]
         {:status 200 :body (in-frame "" (gen-artwork artwork))} 
         {:status 404 :body (gen-404 pid)}))
  (route/resources "/static/")
  (route/not-found "Not Found"))


