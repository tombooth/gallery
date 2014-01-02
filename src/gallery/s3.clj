(ns gallery.s3
  [:require [aws.sdk.s3 :as s3]
            [digest]])

(def s3-config (atom {}))

(defn setup [new-config]
  (reset! s3-config new-config))

(defn ensure-bucket [name]
  (let [conf @s3-config]
    (if (s3/bucket-exists? conf name)
      (s3/create-bucket conf name))))

(defn- generate-key [file]
  (digest/sha-256 file))

(defn upload-file [file]
  (let [conf @s3-config
        key (generate-key file)
        bucket (:bucket-name conf)]
    (s3/put-object conf bucket key file)
    (s3/update-object-acl conf bucket key (s3/grant :all-users :read))
    (str (:base-url conf) "/" key)))


