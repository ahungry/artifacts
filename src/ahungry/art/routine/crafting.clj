(ns ahungry.art.routine.crafting
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]
   [ahungry.art.queue :as queue]
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.routine.bank :as bank]
   [ahungry.art.entity.bank :as ebank]
   [ahungry.art.entity.map :as emap]
   [ahungry.art.entity.craft :as craft]
   [ahungry.art.entity.char :as char]))

(defn sort-by-lowest-skill [character craftables]
  (sort-by (fn [x]
             (let [x (keyword (str (:skill x) "_level"))]
               (get character x)))
           craftables))

(defn get-item-next [name]
  (first (sort-by-lowest-skill
          (char/get-char name)
          (craft/get-all-craftables name))))

(defn get-pref-area [name]
  (let [char (char/get-char name)
        item (get-item-next name)]
    (-> (emap/get-crafting-grounds (:skill item))
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

(def has-craftable-items? craft/has-craftable-items?)

(defn get-quantity-by-code [code xs]
  (-> (filter #(= code (:code %)) xs) first :quantity))

(defn get-iterations [name materials]
  "Given a list of materials, get the total number we can craft, capped
by either inventory space or materials in bank."
  (let [base-quantity (apply + (map :material_quantity materials))
        inv-size (char/get-inventory-size name)
        inv-max (char/get-free-inventory-space inv-size name)
        bank-contents (ebank/get-bank)
        minimal-bank-iterations
        (apply min
               (map
                (fn [mat]
                  (let [bank-quantity (or (get-quantity-by-code (:material_code mat) bank-contents) 0)]
                    (/ bank-quantity (:material_quantity mat))))
                materials))]
    (int (min minimal-bank-iterations (/ inv-max base-quantity)))))

;; In this case, we identified an item we can craft, but most likely,
;; the necessary materials are in the bank - to simplify this, empty
;; the character inventory into the bank as well.
;; TODO: We need to calculate the total crafts we can do so we don't
;; keep moving back and forth between bank and craft station.
(defn do-full-crafting-routine! [name]
  (bank/bank-all-items! name)
  (ebank/import-bank!)
  (let [craft-target (get-item-next name)
        materials (craft/get-materials (:code craft-target))
        iterations (get-iterations name materials)]

    (log/info "Adding a crafting routine to the queue")
    (log/info "Target" (:code craft-target) "of " iterations "iterations"
              "With " (count materials) "unique materials.")

    ;; We should be at the bank already...
    (dorun
     (map
      (fn [{:keys [material_code material_quantity]}]
        (queue/qadd
         name
         {:desc (str "Withdrawing for upgrade: " material_code)
          :fn (fn [] (char/do-bank-withdraw! {:code material_code
                                              :quantity (* iterations material_quantity)} name))}))
      materials))

    ;; Move to the proper craft area
    (queue/qadd
     name
     {:desc (str "Move to crafting area: ")
      :fn (fn [] (when (time-to-move-on? name) (do-move-to-pref-area! name)))})

    ;; Repeat the craft
    (dotimes [_ iterations]
      (queue/qadd
       name
       {:desc (str "Crafting: " (:code craft-target))
        :fn (fn [] (char/do-crafting! {:code (:code craft-target)} name))}))
    ))

(defn routine! [name]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/use-or-rest! name)

    (not (craft/has-craftable-items? name)) (log/info "Routine crafting, nothing to do!")

    ;; See if we should go fight some tougher things
    (time-to-move-on? name) (do-move-to-pref-area! name)

    ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
    true (do-full-crafting-routine! name)
    ;; true (char/do-crafting! {:code (:code (get-item-next name))} name)
    ))
