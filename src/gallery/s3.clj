(ns gallery.s3
  [:require [aws.sdk.s3 :as s3]])

(def s3-config (atom {}))

(defn setup [new-config]
  (reset! s3-config new-config))

(defn ensure-bucket [name]
  (let [conf @s3-config]
    (if (s3/bucket-exists? conf name)
      (s3/create-bucket conf name))))

(defn- generate-key [file]
  ;; need to change this for something like SHA256 of the file
  (str (System/currentTimeMillis) (rand-int 1000)))

(defn upload-file [file]
  (let [conf @s3-config
        key (generate-key file)
        bucket (:bucket-name conf)]
    (s3/put-object conf bucket key file)
    (s3/update-object-acl conf bucket key (s3/grant :all-users :read))
    (str (:base-url conf) "/" key)))


