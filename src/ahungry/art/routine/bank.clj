(ns ahungry.art.routine.bank
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [ahungry.art.entity.bank :as bank]
   [ahungry.art.entity.craft :as craft]
   [ahungry.art.entity.char :as char]))

;; TODO: Make sure none are recyclable either
(defn get-bankable-items [name]
  (let [usable-materials (craft/get-all-usable-materials name)]
    (->> (bank/get-bankable-items name)
         (filter #(not (.contains usable-materials (:code %)))))))

(defn get-item-next [name]
  (first (get-bankable-items name)))

(defn get-pref-area [name]
  (let [char (char/get-char name)
        item (get-item-next name)]
    (-> (emap/get-banking-grounds)
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

(defn has-bankable-items? [name]
  (> (count (get-bankable-items name)) 0))

(defn routine! [name]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/do-rest! name)

    (not (has-bankable-items? name)) (log/info "Routine bank, nothing to do!")

    ;; See if we need to relocate
    (time-to-move-on? name) (do-move-to-pref-area! name)

    ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
    true (let [item (get-item-next name)]
           (char/do-bank-deposit! {:code (:code item)
                                   :quantity (:quantity item)} name))))
