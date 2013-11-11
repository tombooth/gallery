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
     [:link {:rel "stylesheet" :href "//static.artcollective.io/shared.css"}]
     [:link {:rel "stylesheet" :href "/static/ionicons/ionicons.css"}]
     [:link {:rel "stylesheet" :href "/static/main.css"}]]
    [:body
     (concat
      [[:h1
        (if (= title "")
          "pollock."
          [:a {:href "/" :class "local"} "pollock."])
        [:a {:href "//artcollective.io" :class "parent"} "artcollective.io/"]
        [:span {:class "title"} title]]] rest)]]))

(defn gen-artwork [artwork]
  (in-frame (:pid artwork)
     [:img {:src (:url artwork)}]
     [:ul {:class "inspiration"}
      [:li
       [:a {:href (:inspiration_url artwork)}
        [:i {:class "ion-play"}]
        "Listen to the audio that lead to this artwork."]]]
     [:ul {:class "sharing"}
      [:li
       [:a {:href (str "//twitter.com/share?url="
                       (url-encode (str "http://pollock.artcollective.io/"
                                        (:pid artwork)))
                       "&related=tombooth") }
        [:i {:class "ion-social-twitter"}]
        "Share this artwork with you friends."]]]))

(defn gen-404 [pid]
  (in-frame pid
            [:h2 "Artwork not found"]))


(defroutes all-routes
  (GET "/" []
       (in-frame ""
                 [:p {:class "page-middle"}
                  [:p "Call me and leave a message on my number below."]
                  [:p "+44 1290 211866"]]))
  (GET "/:pid" [pid]
       (if-let [artwork (data/get-artwork pid)]
         {:status 200 :body (gen-artwork artwork)} 
         {:status 404 :body (gen-404 pid)}))
  (route/resources "/static/")
  (route/not-found "Not Found"))


