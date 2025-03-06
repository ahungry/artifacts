(ns ahungry.art.routine.foodie
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

(defn get-next-food [name]
  (let [food (first (ebank/get-food))
        inv-space (char/get-free-inventory-space (char/get-inventory-size name) name)]
    (if (not food)
      nil
      (conj food {:quantity (min (:quantity food) inv-space)}))
    ))

(defn has-food-in-bank? [name]
  (get-next-food name))

(defn has-no-food? [name]
  (< (count (char/get-food name)) 1))

(defn do-get-food-routine! [name]
  (bank/bank-all-items! name)
  (let [food-target (get-next-food name)]
    ;; We should be at the bank already...
    (queue/qadd
     name
     {:desc (str "Withdrawing food:" (:code food-target))
      :fn (fn [] (char/do-bank-withdraw! {:code (:code food-target)
                                          :quantity (:quantity food-target)}
                                         name))})
    ))

(defn routine! [name]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/use-or-rest! name)

    true (do-get-food-routine! name)
    ))
