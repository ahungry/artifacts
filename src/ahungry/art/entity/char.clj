(ns ahungry.art.entity.char
  (:require
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]))

(defonce last-char-name (atom nil))

(defn epoch []
  (.getEpochSecond (jt/instant)))

(defn to-epoch [instant]
  (.getEpochSecond (jt/instant instant)))

;; For REPL convenience, save last char name used.
(defn get-name [& [s]]
  (when s (reset! last-char-name s))
  (if-not (or s @last-char-name)
    (throw (Exception. "Need to know the character name!")))
  (or s @last-char-name))

(defn get-count-items [m]
  (apply + (map :quantity m)))

;; Could use select-keys here but this is a nice visual
(defn filter-columns [{:keys [name level max_hp hp x y xp max_xp gold] :as m}]
  {:name name
   :level level
   :attack_fire (:attack_fire m)
   :attack_earth (:attack_earth m)
   :attack_water (:attack_water m)
   :attack_air (:attack_air m)
   :max_hp max_hp
   :hp hp
   :x x
   :y y
   :xp xp
   :max_xp max_xp
   :gold gold
   :woodcutting_level (:woodcutting_level m)
   :woodcutting_xp (:woodcutting_xp m)
   :woodcutting_max_xp (:woodcutting_max_xp m)
   :fishing_level (:fishing_level m)
   :fishing_xp (:fishing_xp m)
   :fishing_max_xp (:fishing_max_xp m)
   :alchemy_level (:alchemy_level m)
   :alchemy_xp (:alchemy_xp m)
   :alchemy_max_xp (:alchemy_max_xp m)
   :mining_level (:mining_level m)
   :mining_xp (:mining_xp m)
   :mining_max_xp (:mining_max_xp m)
   :weaponcrafting_level (:weaponcrafting_level m)
   :weaponcrafting_xp (:weaponcrafting_xp m)
   :weaponcrafting_max_xp (:weaponcrafting_max_xp m)
   :gearcrafting_level (:gearcrafting_level m)
   :gearcrafting_xp (:gearcrafting_xp m)
   :gearcrafting_max_xp (:gearcrafting_max_xp m)
   :jewelrycrafting_level (:jewelrycrafting_level m)
   :jewelrycrafting_xp (:jewelrycrafting_xp m)
   :jewelrycrafting_max_xp (:jewelrycrafting_max_xp m)
   :cooking_level (:cooking_level m)
   :cooking_xp (:cooking_xp m)
   :cooking_max_xp (:cooking_max_xp m)
   :inventory_max_items (:inventory_max_items m)
   :inventory_count_items (get-count-items (:inventory m))
   :weapon_slot (:weapon_slot m)
   :rune_slot (:rune_slot m)
   :shield_slot (:shield_slot m)
   :helmet_slot (:helmet_slot m)
   :body_armor_slot (:body_armor_slot m)
   :leg_armor_slot (:leg_armor_slot m)
   :boots_slot (:boots_slot m)
   :ring1_slot (:ring1_slot m)
   :ring2_slot (:ring2_slot m)
   :amulet_slot (:amulet_slot m)
   :artifact1_slot (:artifact1_slot m)
   :artifact2_slot (:artifact2_slot m)
   :artifact3_slot (:artifact3_slot m)
   :utility1_slot (:utility1_slot m)
   :utility2_slot (:utility2_slot m)
   :bag_slot (:bag_slot m)
   :cooldown_expiration (:cooldown_expiration m)
   :cooldown (:cooldown m)})

(defn progress [{:keys [x y name level xp max_xp hp max_hp gold woodcutting_level woodcutting_xp woodcutting_max_xp]}]
  (log/info
   "Progress" name "lvl:" level ", xp:" xp "/" max_xp ", gold: " gold
   "wood lvl:" woodcutting_level ", xp: " woodcutting_xp "/" woodcutting_max_xp
   ", hp:" hp "/" max_hp)
  (log/info (emap/get-map x y)))

(defn import-char! [char]
  (let [data (filter-columns char)]
    (if (> (count (:name data)) 0)
      (do
        (j/insert! db :chars data)
        (when (> (count (:inventory char)) 0)
          (j/delete! db :inventory [])
          (j/insert-multi!
           db
           :inventory
           (map (fn [inv] (merge {:name (:name char)} inv)) (:inventory char ))))
        (progress data)
        data)
      (log/error "Somehow an empty character import occurred..."))))

(defn fetch-char [name]
  (-> (sdk :get (str "/characters/" name)) :data))

(defn get-char [name]
  (or
   (-> (j/query db ["select * from chars where name = ?" name]) first)
   (import-char! (fetch-char name))))

(defn get-delay [name]
  (- (to-epoch (:cooldown_expiration (get-char name)))
     (epoch)))

(defn get-hp [& [name]]
  (select-keys (get-char (get-name name)) [:hp :max_hp]))

;; Every action returns our character state, so we can follow the pattern here.
(defn do-action! [action & [name body]]
  (let [res ((sdk-for (get-name name)) action body)]
    (if (:error res)
      (do
        (log/error "Action failed - fix the code...")
        (System/exit 1))
      (do
        (log/info "Action success for:" action)
        ;; (log/info "Next delay pre-update: " (get-delay name))
        ;; (log/info "Cooldown data object:" (get-in res [:data :cooldown]))
        ;; (log/info "Cooldown data character:" (get-in res [:data :character :cooldown_expiration]))
        (import-char! (get-in res [:data :character]))
        ;; (log/info "Next delay in: " (get-delay name))
        ;; (log/info "Cooldown data character db:" (:cooldown_expiration (get-char name)))
        ))
    res))

;; 0 arity actions
(def do-rest! (partial do-action! :rest))
(def do-fight! (partial do-action! :fight))
(def do-gather! (partial do-action! :gathering))

(defn do-move! [{:keys [x y]} & [name]]
  (do-action! :move (get-name name) {:x x :y y}))

(defn do-unequip! [{:keys [code slot]} & [name]]
  (do-action! :equip (get-name name) {:slot slot}))

(defn do-equip! [{:keys [code slot]} & [name]]
  (do-action! :equip (get-name name) {:code code :slot slot}))

(defn do-crafting! [{:keys [code]} & [name]]
  (do-action! :crafting (get-name name) {:code code}))

(defn do-recycling! [{:keys [code]} & [name]]
  (do-action! :recycling (get-name name) {:code code}))

(defn get-hunting-grounds [& [name]]
  (let [char (get-char (get-name name))]
    (-> (emap/get-hunting-grounds
         {:hp (:max_hp char)
          :attack (+ (:attack_air char)
                     (:attack_earth char)
                     (:attack_fire char)
                     (:attack_water char))})
        first)))

(defn get-woodcutting-grounds [& [name]]
  (let [char (get-char (get-name name))]
    (-> (emap/get-woodcutting-grounds
         {:woodcutting_level (:woodcutting_level char)})
        first)))

(defn full-health? [name]
  (let [char (get-hp name)]
    (= (:hp char) (:max_hp char))))

(defn can-act? [name]
  (> 0 (get-delay name)))

(defn is-encumbered? [name]
  (let [char (get-char name)]
    (>= (:inventory_count_items char)
        (:inventory_max_items char))))
