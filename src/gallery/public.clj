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
     [:link {:rel "stylesheet" :href "/static/ionicons/ionicons.min.css"}]
     [:link {:rel "stylesheet" :href "/static/main.css"}]]
    [:body
     rest]]))

(def date-formatter (time-format/formatter "MMMMM yyyy"))

(defn gen-inspiration [artwork inspiration]
  [:li
    [:a {:href (str "/" (:pid artwork) "/inspiration/" (:pid inspiration))}
      [:i {:class "ion-play"}]
        "Listen to the inspiration."]])

(defn gen-artwork [artwork]
  [:div {:class "exhibit"}
    [:div {:class "notes"}
     [:h2 [:a {:href "/"} "Pollock"]]
     [:h3 [:a {:href (str "/" (:pid artwork))} (str "Experimental #" (:pid artwork))]]
     (if (:description artwork)
       [:p {:class "description"} (:description artwork)])
     [:p "digital canvas, binary paint."]
     [:p (time-format/unparse date-formatter
                              (time-coerce/from-sql-date (:created artwork)))]
     (if (> (count (:inspiration artwork)) 0)
       [:ul {:class "inspiration"}
         (map #(gen-inspiration artwork %) (:inspiration artwork))])
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
  (GET "/:artwork/inspiration/:inspiration" [artwork inspiration]
       (if-let [inspiration (data/get-inspiration artwork inspiration)]
         {:status 303 :headers {"Location" (:url inspiration)}}
         {:status 404}))
  (route/resources "/static/")
  (route/not-found "Not Found"))


