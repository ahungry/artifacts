(ns ahungry.art.util
  (:require
   [java-time.api :as jt]))

(defn epoch []
  (.getEpochSecond (jt/instant)))

(defn datetime-to-epoch [instant]
  (.getEpochSecond (jt/instant instant)))

;; Character in cooldown: 18.92 seconds left.
(defn get-datetime-from-error [s]
  (->> (re-matches #".*: (.*?) .*" s)
       last
       read-string
       jt/seconds
       (jt/plus (jt/instant))
       .toString))

;; (get-datetime-from-error "Character in cooldown: 18.92 seconds left.")
