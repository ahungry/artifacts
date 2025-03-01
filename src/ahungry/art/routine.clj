(ns ahungry.art.routine
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]
   [ahungry.art.entity.char :as char]
   [ahungry.art.routine.fight :as fight]
   [ahungry.art.routine.mining :as mining]
   [ahungry.art.routine.woodcutting :as woodcutting]
   [ahungry.art.routine.crafting :as crafting]
   [ahungry.art.routine.crafting_upgrades :as crafting_upgrades]
   [ahungry.art.entity.map :as emap]))

(defonce run-routine (atom nil))
(defonce prefer-routine (atom :woodcutting))

(defn do-routine! [name]
  (log/info "Starting routine cycle for " name "preferring" @prefer-routine)
  (reset! run-routine true)
  (future
    (while @run-routine
      (Thread/sleep 1000)
      (when (char/can-act? name)
        (cond
          ;; If encumbered, we can't get more items, so go craft or bank?
          ;; For now, this means convert copper ores into bars
          (char/is-encumbered? name) (crafting/routine! name)

          ;; If we see potential for an item upgrade, go make it and equip it
          (crafting_upgrades/has-craftable-upgrades? name)
          (crafting_upgrades/routine! name)

          ;; Otherwise just default to our preferences
          (= :fighting @prefer-routine) (fight/routine! name)
          (= :mining @prefer-routine) (mining/routine! name)
          (= :woodcutting @prefer-routine) (woodcutting/routine! name)

          true (log/error "Nothing to do - the prefer-routine is wrong..."))
        (log/info "Next action available in: " (char/get-delay name))))
    (log/info "Ending routine cycle for " name)))

(defn stop-routine []
  (reset! run-routine nil))
