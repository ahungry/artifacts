(ns ahungry.art.routine.fight
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]
   [ahungry.art.util :as util]
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [ahungry.art.entity.char :as char]))

;; TODO: Account for additional combat stats like resistances and % improvements
(defn get-pref-area-auto [name]
  (let [char (char/get-char name)]
    (-> (emap/get-hunting-grounds
         {:hp (:max_hp char)
          :level (:level char)
          :attack (+ (:attack_air char)
                     (:attack_earth char)
                     (:attack_fire char)
                     (:attack_water char)
                     10
                     )})
        util/hour-rand)))

(defn get-pref-area [name preferred-mob]
  (if preferred-mob
    (first (emap/get-hunting-grounds-by-mob-code preferred-mob))
    (get-pref-area-auto name)))

(defn do-move-to-pref-area! [name preferred-mob]
  (let [area (get-pref-area name preferred-mob)]
    (char/do-move! area name)))

(defn time-to-move-on? [name preferred-mob]
  (let [pref-area (get-pref-area name preferred-mob)
        char (char/get-char name)]
    (or (not= (:x pref-area) (:x char))
        (not= (:y pref-area) (:y char)))))

(defn routine! [name & [{:keys [preferred-mob]}]]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/use-or-rest! name)
    ;; See if we should go fight some tougher things
    (time-to-move-on? name preferred-mob) (do-move-to-pref-area! name preferred-mob)
    ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
    true (char/do-fight! name)))
