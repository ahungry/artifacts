(ns ahungry.art.entity.bank
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-bank []
  (j/query db ["select * from bank order by quantity desc"]))

(defn get-bankable-items [name]
  (j/query db ["select * from inventory
where name=? and code <> '' and quantity > 0 order by quantity desc" name]))

(defn get-bank-types []
  (j/query db ["select distinct(type) from bank"] {:row-fn :type}))

(defn filter-columns [m]
  (select-keys m [:code :quantity]))

(defn inspect [x]
  (log/info "Found a set of data with " (count x) " elements.")
  x)

(defn import-bank! []
  (let [pages (-> (sdk :get "/my/bank/items") :pages)]
    (log/info "About to fetch " pages "pages of bank data!")
    ;; (j/delete! db :bank [])
    (dorun
     (for [page (map inc (range pages))]
       (let [res (sdk :get (str "/my/bank/items?page=" page))]
         (->> res :data
              inspect
              (map filter-columns)
              (j/insert-multi! db :bank))
         nil)))))
