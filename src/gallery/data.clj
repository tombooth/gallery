(ns gallery.data
  [:require [korma.core :refer :all]
            [korma.db :refer :all]
            [hashids.core :as hashids]])

(def hashids-salt "gallery")

(defentity users)

(defentity artworks)

(defn setup-db [spec]
  (let [conn (create-db spec)]
    (default-connection conn)
    conn))

(defn get-user [id]
  (first
   (select users
           (where {:id id}))))

(defn create-user [id]
  (let [user {:id id}]
    (insert users (values user))
    user))

(defn get-or-create-user [id]
  (let [user (get-user id)]
    (if (nil? user)
      (create-user id)
      user)))

(defn valid-artwork [in]
  (if (or (nil? (:url in))
          (nil? (:inspiration_url in)))
    nil
    {:url (:url in)
     :inspiration_url (:inspiration_url in)}))

(defn add-pid-to-artwork [artwork]
  (dissoc (assoc artwork 
                 :pid 
                 (hashids/encrypt (:id artwork) hashids-salt))
          :id))

(defn- store-artwork [artwork]
  (-> (transaction (insert artworks (values artwork))
                   (select artworks (aggregate (max :id) :id)))
      first
      :id))

(defn add-artwork [user artwork]
  (let [db-artwork (assoc artwork :user_id (:id user))
        id (store-artwork db-artwork)
        pid (hashids/encrypt id hashids-salt)]
    (assoc db-artwork :pid pid)))

(defn get-artwork [pid]
  (let [id (hashids/decrypt pid hashids-salt)]
    (if-let [artwork (first (select artworks (where {:id id})))]
      (add-pid-to-artwork artwork))))

(defn get-recent-artworks [count]
  (map add-pid-to-artwork
       (select artworks
               (order :created :DESC)
               (limit count))))
