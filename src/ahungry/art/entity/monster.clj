(ns ahungry.art.entity.monster
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-monsters []
  (j/query db ["select * from monsters where 1=?" 1]))

(defn get-monster [code]
  (j/query db ["select * from monsters where code = ?" code]))

(defn filter-columns [{:keys [code level hp] :as m}]
  {:code code
   :level level
   :hp hp
   :attack_fire (:attack_fire m)
   :attack_earth (:attack_earth m)
   :attack_water (:attack_water m)
   :attack_air (:attack_air m)
   })

(defn import-monsters! []
  ;; (j/delete! db :monsters [])
  (->> (sdk :get "/monsters") :data
       (map filter-columns)
       (j/insert-multi! db :monsters)))
