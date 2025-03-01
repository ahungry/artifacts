(ns ahungry.art.entity.item
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-item [code]
  (j/query db ["select * from items where code=?" code]))

(defn get-items []
  (j/query db ["select * from items where 1=?" 1]))

(defn get-item-types []
  (j/query db ["select distinct(type) from items"] {:row-fn :type}))

(defn is-additive? [{:keys [code]}]
  (.contains ["hp" "attack_fire" "attack_earth" "attack_air" "attack_water"] code))

(defn is-multiplicative? [{:keys [code]}]
  (.contains ["res_water" "res_air" "res_earth" "res_fire" "critical_strike"] code))

(defn get-weapon-quality [xs]
  (let [additives (filter is-additive? xs)
        multiplicatives (filter is-multiplicative? xs)
        base (apply + (map :value additives))
        mult (apply + (map :value multiplicatives))]
    (* base (+ 1 (float (/ mult 100))))))

(defn get-armor-quality [xs]
  (let [additives (filter is-additive? xs)
        multiplicatives (filter is-multiplicative? xs)
        base (apply + (map :value additives))
        mult (apply + (map :value multiplicatives))]
    (* base (+ 1 (float (/ mult 100))))))

(defn get-quality [m]
  (cond
    (= "weapon" (:type m)) (get-weapon-quality (:effects m))
    true 0))

(defn filter-columns [m]
  {
   :quality (get-quality m)
   :code (:code m)
   :name (:name m)
   :level (:level m)
   :type (:type m)
   :subtype (:subtype m)
   :description (:description m)
   :tradeable (:tradeable m)})

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
