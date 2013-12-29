(ns gallery.schema
  [:require [lobos.core :as lobos]
            [lobos.schema :as schema]
            [lobos.connectivity :as connectivity]])


(defn create-all []
  (lobos/create
   (schema/table :users
                 (schema/char :id 64 :primary-key)))
  (lobos/create
   (schema/table :artworks
                 (schema/integer :id :auto-inc :primary-key)
                 (schema/char :user_id [:refer :users :id :on-delete :set-null])
                 (schema/varchar :url 512 :not-null)
                 (schema/varchar :description 1000)
                 (schema/varchar :config 5000)
                 (schema/timestamp :created (schema/default (now)))))
  (lobos/create
   (schema/table :inspiration
                 (schema/integer :id :auto-inc :primary-key)
                 (schema/integer :artworks_id [:refer :artworks :id :on-delete :set-null])
                 (schema/varchar :url 512 :not-null)
                 (schema/varchar :mime_type 50 :not-null)
                 (schema/timestamp :created (schema/default (now))))))

(defn drop-all []
  (lobos/drop
   (schema/table :users))
  (lobos/drop
   (schema/table :artworks))
  (lobos/drop
   (schema/table :inspiration)))

(defn exec [spec fn]
  (connectivity/with-spec-connection
    spec fn))

