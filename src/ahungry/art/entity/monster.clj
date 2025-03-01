(ns ahungry.art.entity.monster
  (:require
   [ahungry.art.entity.db :as db]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]))

(defn get-monsters []
  (j/query db/db ["select * from monsters where 1=?" 1]))

(get-monsters)
