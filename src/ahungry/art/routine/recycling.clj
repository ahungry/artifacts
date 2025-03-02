(ns ahungry.art.routine.recycling
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

(defn get-item-next [name]
  (first (craft/get-recyclables name)))

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

(def has-recyclables? craft/has-recyclables?)

(defn routine! [name]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/do-rest! name)

    (not (craft/has-recyclables? name)) (log/info "Routine recycling, nothing to do!")

    ;; See if we need to relocate
    (time-to-move-on? name) (do-move-to-pref-area! name)

    ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
    true (char/do-recycling! {:code (:code (get-item-next name))} name)))
