(ns ahungry.art.entity.effect
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-effect [code]
  (j/query db ["select * from effects where code=?" code]))

(defn get-effects []
  (j/query db ["select * from effects where 1=?" 1]))

(defn filter-columns [m]
  (select-keys m [:code :name :description :type :subtype]))

(defn inspect [x]
  (log/info "Found a set of data with " (count x) " elements.")
  x)

(defn fetch-effect [code]
  (-> (sdk :get (str "/effects/" code)) :data))

(defn import-effects! []
  (let [pages (-> (sdk :get "/effects") :pages)]
    (log/info "About to fetch " pages "pages of effect data!")
    (j/delete! db :effects [])
    (dorun
     (for [page (map inc (range pages))]
       (let [res (sdk :get (str "/effects?page=" page))]
         (->> res :data
              inspect
              (map filter-columns)
              (j/insert-multi! db :effects))
         nil)))))
