(ns ahungry.art.entity.map
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-map [x y]
  (j/query db ["select * from maps where x=? and y=?" x y]))

(defn get-maps []
  (j/query db ["select * from maps where 1=?" 1]))

(defn get-maps-by-content-type [type]
  (j/query db ["select * from maps left join monsters m ON maps.content_code = m.code where content_type = ?" type]))

(defn get-monster-maps []
  (get-maps-by-content-type "monster"))

(defn get-hunting-grounds-by-mob-code [code]
  (j/query
   db
   ["
select * from maps
left join monsters m ON maps.content_code = m.code
where content_type = 'monster'
and m.code = ? "

    code]))

(defn get-hunting-grounds [{:keys [hp attack level] :as char}]
  (j/query
   db
   ["
select * from maps
left join monsters m ON maps.content_code = m.code
where content_type = 'monster'
and (m.hp / ?) < (? / (m.attack_fire + m.attack_earth + m.attack_water + m.attack_air))
and (level + 10) >= ?
group by m.code
order by level desc
"
    attack hp level]))

(defn get-woodcutting-grounds [{:keys [woodcutting_level] :as char}]
  (j/query
   db
   ["
select * from maps
left join resources r ON maps.content_code = r.code
where content_type = 'resource'
and r.level <= ?
and r.skill = 'woodcutting'
order by level desc
"
    woodcutting_level]))

(defn get-mining-grounds [{:keys [mining_level] :as char}]
  (j/query
   db
   ["
select * from maps
left join resources r ON maps.content_code = r.code
where content_type = 'resource'
and r.level <= ?
and r.skill = 'mining'
order by level desc
"
    mining_level]))

(defn get-crafting-grounds [type]
  (j/query
   db
   ["
select * from maps
where content_type = 'workshop'
and content_code = ?
"
    type]))

(defn get-banking-grounds []
  (j/query
   db
   ["
select * from maps
where content_type = 'bank' "

    ]))

(defn filter-columns [{:keys [name skin x y] :as m}]
  {:name name
   :skin skin
   :x x
   :y y
   :content_type (-> m :content :type)
   :content_code (-> m :content :code)
   })

(defn inspect [x]
  (log/info "Found a set of data with " (count x) " elements.")
  x)

(defn import-maps! []
  (let [pages (-> (sdk :get "/maps") :pages)]
    (log/info "About to fetch " pages "pages of map data!")
    ;; (j/delete! db :maps [])
    (dorun
     (for [page (map inc (range pages))]
       (let [res (sdk :get (str "/maps?page=" page))]
         (->> res :data
              inspect
              (map filter-columns)
              (j/insert-multi! db :maps))
         nil)))))
