(ns gallery.data
  [:require [korma.core :refer :all]
            [korma.db :refer :all]
            [hashids.core :as hashids]])

(def hashids-salt "gallery")

(defentity users)

(defentity artworks)

(defn setup-db [db server user password]
  (let [conn (create-db (postgres {:db db :user user :password password :host server}))]
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

(defn add-artwork [user artwork]
  (let [db-artwork (assoc artwork :user_id (:id user))
        stored-artwork (insert artworks (values db-artwork))
        pid (hashids/encrypt (:id stored-artwork) hashids-salt)
        pid-artwork (update artworks
                            (set-fields {:pid pid})
                            (where {:id (:id stored-artwork)}))]
    pid-artwork))

(defn add-pid-to-artwork [artwork]
  (dissoc (assoc artwork 
                 :pid 
                 (hashids/encrypt (:id artwork) hashids-salt))
          :id))

(defn get-artwork [pid]
  (let [id (hashids/decrypt pid hashids-salt)]
    (add-pid-to-artwork (first (select artworks (where {:id id}))))))

(defn get-recent-artworks [count]
  (map add-pid-to-artwork
       (select artworks
               (order :created :DESC)
               (limit count))))
