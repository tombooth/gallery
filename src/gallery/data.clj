(ns gallery.data
  [:require [korma.core :refer :all]
            [korma.db :refer :all]
            [hashids.core :as hashids]])

(def hashids-salt "gallery")

(defentity inspiration)

(defentity artworks
  (has-many inspiration))

(defentity users)

(defn setup-db [spec]
  (let [conn (create-db spec)]
    (default-connection conn)
    conn))

(defn add-pid-to-row [row]
  (dissoc (assoc row
                 :pid 
                 (hashids/encrypt (:id row) hashids-salt))
          :id))

(defn- store-row [table row]
  (-> (transaction (insert table (values row))
                   (select table (aggregate (max :id) :id)))
      first
      :id))

;; Users

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

;; Artworks

(defn- add-pids-to-artwork [artwork]
  (add-pid-to-row (assoc artwork :inspiration
                         (map add-pid-to-row (:inspiration artwork)))))

(defn valid-artwork [in]
  (if (nil? (:url in))
    nil
    {:url (:url in)
     :description (:description in)}))

(defn add-artwork [user artwork]
  (let [db-artwork (assoc artwork :user_id (:id user))
        id (store-row artworks db-artwork)
        pid (hashids/encrypt id hashids-salt)]
    (assoc db-artwork :pid pid)))

(defn get-artwork [pid]
  (let [id (hashids/decrypt pid hashids-salt)]
    (if-let [artwork (first (select artworks
                                    (with inspiration)
                                    (where {:id id})))]
      (add-pids-to-artwork artwork))))

(defn get-recent-artworks [count]
  (map add-pids-to-artwork
       (select artworks
               (with inspiration)
               (order :created :DESC)
               (limit count))))

;; Inspiration

(defn verify-inspiration [hash]
  (if (or (nil? (:url hash))
          (nil? (:mime_type hash)))
    nil
    {:url (:url hash)
     :mime_type (:mime_type hash)}))

(defn add-inspiration [artwork-pid hash]
  (if (not (nil? artwork-pid))
    (let [artwork-id (hashids/decrypt artwork-pid hashids-salt)
          db-inspiration (assoc hash :artworks_id artwork-id)
          id (store-row inspiration db-inspiration)
          pid (hashids/encrypt id hashids-salt)]
      (assoc hash :pid pid))))

(defn get-inspiration [artwork-pid inspiration-pid]
  (let [artwork-id (hashids/decrypt artwork-pid hashids-salt)
        inspiration-id (hashids/decrypt inspiration-pid hashids-salt)]
    (first (select inspiration (where {:id inspiration-id :artworks_id artwork-id})))))

