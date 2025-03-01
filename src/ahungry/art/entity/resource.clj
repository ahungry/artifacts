(ns ahungry.art.entity.resource
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]))

(defn get-resources []
  (j/query db ["select * from resources where 1=?" 1]))

(defn get-resource [code]
  (j/query db ["select * from resources where code = ?" code]))

(defn filter-columns [m]
  (select-keys m [:code :skill :level]))

(defn import-resources! []
  ;; (j/delete! db :resources [])
  (->> (sdk :get "/resources") :data
       (map filter-columns)
       (j/insert-multi! db :resources)))
