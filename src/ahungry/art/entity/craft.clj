(ns ahungry.art.entity.craft
  (:require
   [ahungry.art.repo :refer [db sdk]]
   [ahungry.art.entity.char :as c]
   [ahungry.art.entity.item :as i]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-craft [code]
  (j/query db ["select c.*, i.type, i.subtype, i.quality from crafts c
left join items i on c.code = i.code where c.code=?" code]))

(defn get-craft-codes []
  (j/query db ["select distinct(c.code) from crafts c
left join items i on c.code = i.code where 1=?" 1] {:row-fn :code}))

(defn has-material-in-inventory? [name {:keys [material_code material_quantity]}]
  (= 1 (count (j/query db ["select * from inventory where name=? and code=? and quantity>=?"
                           name material_code material_quantity]))))

(defn has-materials? [name craft]
  (let [reagent-count (count craft)
        has-it? (partial has-material-in-inventory? name)
        inventory-matches (filter has-it? craft)
        inventory-count (count inventory-matches)]
    (= inventory-count reagent-count)))

(defn get-skill-level [reagent]
  (keyword (str (:skill reagent) "_level")))

(defn has-skill? [name craft]
  (let [reagent-count (count craft)
        char (c/get-char name)
        skill-count (->> (filter (fn [reagent]
                                   (>= ((get-skill-level reagent) char)
                                       (:level reagent))) craft) count)]
    (= skill-count reagent-count)))

(defn get-all-usable-materials [name]
  (->> (get-craft-codes)
       (map get-craft)
       (filter (partial has-skill? name))
       flatten
       (map #(:material_code %))
       set))

(defn get-all-craftables [name]
  (->> (get-craft-codes)
       (map get-craft)
       (filter (partial has-skill? name))
       (filter (partial has-materials? name))
       (map first)
       flatten))

;; +------------+
;; |    type    |
;; +------------+
;; | amulet     |
;; | artifact   |
;; | bag        |
;; | body_armor |
;; | boots      |
;; | consumable |
;; | currency   |
;; | helmet     |
;; | leg_armor  |
;; | resource   |
;; | ring       |
;; | rune       |
;; | shield     |
;; | utility    |
;; | weapon     |
;; +------------+
(defn is-better-than-equipped? [char item]
  (let [equipped-item-lookup
        (cond
          (= "body_armor" (:type item)) :body_armor_slot
          (= "leg_armor" (:type item)) :leg_armor_slot
          (= "boots" (:type item)) :boots_slot
          (= "helmet" (:type item)) :helmet_slot
          (= "ring" (:type item)) :ring_slot
          (= "shield" (:type item)) :shield_slot
          (= "rune" (:type item)) :rune_slot
          (= "weapon" (:type item)) :weapon_slot)
        existing-item (first (i/get-item (equipped-item-lookup char)))]
    (if existing-item
      (> (:quality item) (:quality existing-item))
      true)))

(defn get-craftable-upgrades [name]
  (let [char (c/get-char name)]
    (->> (get-all-craftables name)
         (filter #(> (:quality %) 0))
         (filter (partial is-better-than-equipped? char)))))

(defn get-recyclables [name]
  (j/query db ["
select i.*, c.skill from inventory i
left join items it on i.code = it.code
left join crafts c on i.code = c.code
where i.name = ?
and it.type IN ('weapon', 'boots', 'helmet', 'shield', 'leg_armor', 'body_armor')
and i.code <> ''
and i.code not in (select distinct(material_code) from crafts)"
               name]))

(defn has-craftable-items? [name]
  (> (count (get-all-craftables name)) 0))

(defn has-craftable-upgrades? [name]
  (> (count (get-craftable-upgrades name)) 0))

(defn has-recyclables? [name]
  (> (count (get-recyclables name)) 0))

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
