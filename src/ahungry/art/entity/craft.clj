(ns ahungry.art.entity.craft
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [ahungry.art.entity.craft :as craft]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-craft [code]
  (j/query db ["select * from crafts where code=?" code]))

(defn get-crafts []
  (j/query db ["select * from crafts where 1=?" 1]))

(defn insert-craft-rows! [m]
  (when (:craft m)
    (let [code (:code m)
          skill (:skill (:craft m))
          level (:level (:craft m))
          quantity (:quantity (:craft m))
          ]
      (->> (map (fn [material]
                  {:code code
                   :skill skill
                   :level level
                   :quantity quantity
                   :material_code (:code material)
                   :material_quantity (:quantity material)}) (:items (:craft m)))
           (j/insert-multi! db :crafts)
           doall))))

(defn inspect [x]
  (log/info "Found a set of data with " (count x) " elements.")
  x)

(defn import-crafts! []
  (let [pages (-> (sdk :get "/items") :pages)]
    (log/info "About to fetch " pages "pages of craft data!")
    (j/delete! db :crafts [])
    (for [page (map inc (range pages))]
             (let [res (sdk :get (str "/items?page=" page))]
               (->> res :data
                    inspect
                    (map insert-craft-rows!)
                    doall)
               nil))))
