(ns gallery.public
  [:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [hiccup.util :refer [url-encode]]
            [clj-time.coerce :as time-coerce]
            [clj-time.format :as time-format]
            [gallery.data :as data]])

(defn in-frame [& rest]
  (html
   "<!DOCTYPE html>"
   [:html
    [:head
     [:title "pollock.artcollective.io"]
     [:link {:rel "stylesheet" :href "/static/ionicons/ionicons.css"}]
     [:link {:rel "stylesheet" :href "/static/main.css"}]]
    [:body
     rest]]))

(def date-formatter (time-format/formatter "MMMMM yyyy"))

(defn gen-artwork [artwork]
  [:div {:class "exhibit"}
    [:div {:class "notes"}
     [:h2 [:a {:href "/"} "Pollock"]]
     [:h3 [:a {:href (str "/" (:pid artwork))} (str "Experimental #" (:pid artwork))]]
     [:p "Some blurb about how this piece of work was made."]
     [:p "digital canvas, binary paint."]
     [:p (time-format/unparse date-formatter
                              (time-coerce/from-sql-date (:created artwork)))]
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
  (in-frame [:h2 "Artwork not found"]))

(defn gen-home []
  (in-frame
   [:h1 [:b "Pollock: "] "Experimental Series - Audio"]
   [:p "A set of generated art works using audio anaysis of clips derived from voicemail left on the telephone number below."]
   [:p {:class "voicemail"} "Please call " [:b "+441290211866"] " to get your own artwork."]
   (map gen-artwork (data/get-recent-artworks 5))))

(defroutes all-routes
  (GET "/" [] (gen-home))
  (GET "/:pid" [pid]
       (if-let [artwork (data/get-artwork pid)]
         {:status 200 :body (in-frame (gen-artwork artwork))} 
         {:status 404 :body (gen-404 pid)}))
  (route/resources "/static/")
  (route/not-found "Not Found"))


