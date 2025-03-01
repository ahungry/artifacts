(ns ahungry.art.entity.item
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [ahungry.art.entity.craft :as craft]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-item [code]
  (j/query db ["select * from items where code=?" code]))

(defn get-items []
  (j/query db ["select * from items where 1=?" 1]))

(defn filter-columns [m]
  (select-keys m [:code :name :level :type :subtype :description :tradeable]))

(defn inspect [x]
  (log/info "Found a set of data with " (count x) " elements.")
  x)

(defn import-items! []
  (let [pages (-> (sdk :get "/items") :pages)]
    (log/info "About to fetch " pages "pages of item data!")
    ;; (j/delete! db :items [])
    (for [page (map inc (range pages))]
             (let [res (sdk :get (str "/items?page=" page))]
               (->> res :data
                    inspect
                    (map filter-columns)
                    (j/insert-multi! db :items)
                    doall)
               nil))))
