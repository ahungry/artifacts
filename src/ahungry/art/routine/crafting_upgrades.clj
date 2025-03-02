(ns ahungry.art.routine.crafting_upgrades
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [ahungry.art.entity.craft :as craft]
   [ahungry.art.entity.char :as char]))

(defonce pending-equippables (atom {}))

(defn get-item-upgrade [name]
  (first (craft/get-craftable-upgrades name)))

(defn get-pref-area [name]
  (let [char (char/get-char name)
        item-upgrade (get-item-upgrade name)]
    (-> (emap/get-crafting-grounds (:skill item-upgrade))
        first)))

(defn do-move-to-pref-area! [name]
  (let [area (get-pref-area name)]
    (char/do-move! area name)))

(defn time-to-move-on? [name]
  (let [pref-area (get-pref-area name)
        char (char/get-char name)]
    (and pref-area
         (or (not= (:x pref-area) (:x char))
             (not= (:y pref-area) (:y char))))))

(defn do-crafting! [name]
  (let [item (get-item-upgrade name)]
    (swap! pending-equippables conj {name item})
    (char/do-crafting! {:code (:code item)} name)))

(defn has-pending-equippable? [name]
  (get @pending-equippables name))

(defn equip-pending-item [name]
  (let [item (get @pending-equippables name)]
    (log/info "Swapping gear..." item)
    (char/do-unequip! {:slot (:type item)} name)
    (char/do-equip! {:slot (:type item) :code (:code item)} name)
    ;; TODO: Need to confirm this actually worked before giving up...
    (swap! pending-equippables conj {name nil})))

(def has-craftable-upgrades? craft/has-craftable-upgrades?)

(defn routine! [name]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/do-rest! name)

    (has-pending-equippable? name) (equip-pending-item name)

    (not (craft/has-craftable-upgrades? name)) (log/info "Routine crafting_upgrades, nothing to do!")

    ;; See if we need to move the target
    (time-to-move-on? name) (do-move-to-pref-area! name)

    ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
    true (do-crafting! name)))
