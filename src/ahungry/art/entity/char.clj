(ns ahungry.art.entity.char
  (:require
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [java-time.api :as jt]))

(defonce last-char-name (atom nil))
(defonce run-actions (atom nil))

(defn epoch []
  (.getEpochSecond (jt/instant)))

(defn to-epoch [instant]
  (.getEpochSecond (jt/instant instant)))

;; For REPL convenience, save last char name used.
(defn get-name [& [s]]
  (when s (reset! last-char-name s))
  (if-not (or s @last-char-name)
    (throw (Exception. "Need to know the character name!")))
  (or s @last-char-name))

;; Could use select-keys here but this is a nice visual
(defn filter-columns [{:keys [name level max_hp hp x y xp max_xp gold] :as m}]
  {:name name
   :level level
   :attack_fire (:attack_fire m)
   :attack_earth (:attack_earth m)
   :attack_water (:attack_water m)
   :attack_air (:attack_air m)
   :max_hp max_hp
   :hp hp
   :x x
   :y y
   :xp xp
   :max_xp max_xp
   :gold gold
   :cooldown_expiration (:cooldown_expiration m)
   :cooldown (:cooldown m)})

(defn progress [{:keys [name level xp max_xp hp max_hp gold]}]
  (log/info
   "Progress" name "lvl:" level ", xp:" xp "/" max_xp ", gold: " gold
   ", hp:" hp "/" max_hp))

(defn import-char! [char]
  (let [data (filter-columns char)]
    (j/insert! db :chars data)
    (progress data)
    data))

(defn fetch-char [name]
  (-> (sdk :get (str "/characters/" name)) :data))

(defn get-char [name]
  (or
   (-> (j/query db ["select * from chars where name = ?" name]) first)
   (import-char! (fetch-char name))))

(defn get-delay [name]
  (- (to-epoch (:cooldown_expiration (get-char name)))
     (epoch)))

(defn get-hp [& [name]]
  (select-keys (get-char (get-name name)) [:hp :max_hp]))

;; Every action returns our character state, so we can follow the pattern here.
(defn do-action! [action & [name body]]
  (let [res ((sdk-for (get-name name)) action body)]
    (import-char! (get-in res [:data :character]))
    res))

;; 0 arity actions
(def do-rest! (partial do-action! :rest))
(def do-fight! (partial do-action! :fight))

(defn do-move [{:keys [x y]} & [name]]
  ((sdk-for (get-name name)) :move {:x x :y y}))

(defn get-hunting-grounds [& [name]]
  (let [char (get-char (get-name name))]
    (-> (emap/get-hunting-grounds
         {:hp (:max_hp char)
          :attack (+ (:attack_air char)
                     (:attack_earth char)
                     (:attack_fire char)
                     (:attack_water char))})
        last)))

(defn do-move-to-hunting-grounds [& [name]]
  (let [area (get-hunting-grounds (get-name name))]
    (do-move area (get-name name))))

(defn full-health? [name]
  (let [char (get-hp name)]
    (= (:hp char) (:max_hp char))))

(defn can-act? [name]
  (> 0 (get-delay name)))

;; Our get-hunting-grounds will recommend the highest difficulty area
;; our character can take on - if it's not our current time, time to move.
(defn time-to-move-on? [name]
  (let [best-area (get-hunting-grounds "ahungry")
        char (get-char name)]
    (or (not= (:x best-area) (:x char))
        (not= (:y best-area) (:y char)))))

(defn do-actions! [name]
  (log/info "Starting action cycle for " name)
  (reset! run-actions true)
  (future
    (while @run-actions
      (Thread/sleep 1000)
      (when (can-act? name)
        (cond
          ;; Anytime we aren't full health, resting takes precedence.
          (not (full-health? name)) (do-rest! name)
          ;; See if we should go fight some tougher things
          (time-to-move-on? name) (do-move-to-hunting-grounds name)
          ;; TODO: Make the default action a priority based thing? (fight vs craft vs events)
          true (do-fight! name))
        (log/info "Next action available in: " (get-delay name))))
    (log/info "Ending action cycle for " name)))

(defn stop-actions []
  (reset! run-actions nil))
