(ns ahungry.art.routine.woodcutting
  (:require
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [ahungry.art.entity.char :as char]))

(defn get-pref-area [name]
  (let [char (char/get-char name)]
    (-> (emap/get-woodcutting-grounds
         {:woodcutting_level (min (:woodcutting_level char)
                                  ;; (:gearcrafting_level char)
                                  ;; (:weaponcrafting_level char)
                                  )})
        ;; Highest level
        first
        ;; Lowest level
        ;; last
        )))

(defn do-move-to-pref-area! [name]
  (let [area (get-pref-area name)]
    (char/do-move! area name)))

(defn time-to-move-on? [name]
  (let [pref-area (get-pref-area name)
        char (char/get-char name)]
    (or (not= (:x pref-area) (:x char))
        (not= (:y pref-area) (:y char)))))

(defn routine! [name]
  (cond
    ;; Anytime we aren't full health, resting takes precedence.
    (not (char/full-health? name)) (char/use-or-rest! name)
    ;; See if we should go fight some tougher things
    (time-to-move-on? name) (do-move-to-pref-area! name)
    ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
    true (char/do-gather! name)))
