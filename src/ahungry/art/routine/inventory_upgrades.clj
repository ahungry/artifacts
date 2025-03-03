(ns ahungry.art.routine.inventory_upgrades
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
  (first (char/get-equippable-upgrades name)))

(defn has-equippable-upgrades? [name]
  (> (count (char/get-equippable-upgrades name)) 0))

(defn equip-pending-item [name]
  (let [item (get-item-upgrade name)]
    (log/info "Swapping gear..." item)
    ;; If existing_quality is -1, we already unequipped and can equip the best item
    (if (= -1 (:existing_quality item))
      (char/do-equip! {:slot (:slot_type item) :code (:code item)} name)
      (char/do-unequip! {:slot (:slot_type item)} name))))

(defn routine! [name]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/do-rest! name)

    (has-equippable-upgrades? name) (equip-pending-item name)

    (not (has-equippable-upgrades? name)) (log/info "Routine inventory_upgrades, nothing to do!" )

    ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
    true (log/info "Routine inventory_upgrades, nothing to do!")))
