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

(defn get-hunting-grounds [{:keys [hp attack] :as char}]
  (j/query
   db
   ["
select * from maps
left join monsters m ON maps.content_code = m.code
where content_type = 'monster'
and (m.hp / ?) < (? / (m.attack_fire + m.attack_earth + m.attack_water + m.attack_air))
order by level desc
"
    attack hp]))

(defn filter-columns [{:keys [name skin x y] :as m}]
  {:name name
   :skin skin
   :x x
   :y y
   :content_type (-> m :content :type)
   :content_code (-> m :content :code)
   })

(defn inspect [x]
  (log/debug "Found a set of data with " (count x) " elements.")
  x)

(defn import-maps! []
  (let [pages (-> (sdk :get "/maps") :pages)]
    (log/info "About to fetch " pages "pages of map data!")
    ;; (j/delete! db :maps [])
    (for [page (map inc (range pages))]
             (let [res (sdk :get (str "/maps?page=" page))]
               (->> res :body :data
                    inspect
                    (map filter-columns)
                    (j/insert-multi! db :maps))
               nil))))
