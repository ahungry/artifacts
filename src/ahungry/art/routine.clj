(ns ahungry.art.routine
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]
   [ahungry.art.queue :as queue]
   [ahungry.art.entity.char :as char]
   [ahungry.art.routine.fight :as fight]
   [ahungry.art.routine.mining :as mining]
   [ahungry.art.routine.woodcutting :as woodcutting]
   [ahungry.art.routine.bank :as bank]
   [ahungry.art.routine.recycling :as recycling]
   [ahungry.art.routine.crafting :as crafting]
   [ahungry.art.routine.crafting_upgrades :as crafting_upgrades]
   [ahungry.art.routine.inventory_upgrades :as inventory_upgrades]
   [ahungry.art.entity.map :as emap]))

(defonce run-routine (atom nil))

(defn get-random-routine-pref []
  (let [opts [:fighting :mining :woodcutting]]
    (nth opts (rand-int (count opts)))))

(defn is-allowed? [xs x]
  (not (.contains xs x)))

(defn do-routine! [{:keys [name preferred-routine forbidden-routines]}]
  (let [prefer-routine (atom preferred-routine)
        is-allowed? (partial is-allowed? forbidden-routines)]

    (reset! run-routine true)

    ;;  Rotate through routines every 5 minutes
    (when (= :auto @prefer-routine)
      (future
        (while @run-routine
          (reset! prefer-routine (get-random-routine-pref))
          (log/info "New random routine preference chosen!" @prefer-routine)
          (Thread/sleep (* 1000 60 15)))))

    (future
      (while @run-routine
        (Thread/sleep 1000)
        (when (char/can-act? name)
          (cond
            (queue/has-actions? name)
            (do
              (log/info "QUEUE ACTION RESPONSIBLE FOR EXECUTION!")
              (queue/show)
              (queue/do-next-action name))

            ;; If encumbered, we can't get more items, so go craft or bank?
            ;; For now, this means convert copper ores into bars
            ;; (char/is-encumbered? name) (crafting/routine! name)
            (and (char/is-close-to-encumbered? name)
                 (bank/has-bankable-items? name))
            (bank/routine! name)

            ;; Conditional options
            (and (is-allowed? :equipping)
                 (inventory_upgrades/has-equippable-upgrades? name))
            (inventory_upgrades/routine! name)

            ;; Maybe we can do some recycling?
            (and (is-allowed? :recycling)
                 (recycling/has-recyclables? name))
            (recycling/routine! name)

            ;; If we see potential for an item upgrade, go make it and equip it
            (and (is-allowed? :equipping)
                 (crafting_upgrades/has-craftable-upgrades? name))
            (crafting_upgrades/routine! name)

            ;; Maybe we can craft for some skill ups...
            (and (is-allowed? :crafting)
                 (not (char/too-much-food? name))
                 (crafting/has-craftable-items? name))
            (crafting/routine! name)

            ;; Non-conditional options
            (and (is-allowed? :fighting)
                 (= :fighting @prefer-routine)) (fight/routine! name)

            (and (is-allowed? :mining)
                 (= :mining @prefer-routine)) (mining/routine! name)

            (and (is-allowed? :woodcutting)
                 (= :woodcutting @prefer-routine)) (woodcutting/routine! name)

            true (log/error "Nothing to do - the prefer-routine is wrong..." name @prefer-routine))
          ;; (log/info "Next action available in: " (char/get-delay name) name)
          ))
      (log/info "Ending routine cycle for " name))))

(defn stop-routine []
  (reset! run-routine nil))
