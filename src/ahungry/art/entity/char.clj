(ns ahungry.art.entity.char
  (:require
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn fetch-char [name]
  (-> (sdk :get (str "/characters/" name)) :data))

(defn get-char [name]
  (-> (j/query db ["select * from chars where name = ?" name]) first))

(defn get-hp [& [name]]
  (select-keys (get-char (get-name name)) [:hp :max_hp]))

;; Could use select-keys here but this is a nice visual
(defn filter-columns [{:keys [name level max_hp hp x y] :as m}]
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
   :cooldown_expiration (:cooldown_expiration m)
   :cooldown (:cooldown m)})

(defn import-char! [char]
  (j/insert! db :chars (filter-columns char)))

(defonce last-char-name (atom nil))

;; For REPL convenience, save last char name used.
(defn get-name [& [s]]
  (when s (reset! last-char-name s))
  (if-not (or s @last-char-name)
    (throw (Exception. "Need to know the character name!")))
  (or s @last-char-name))

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
    (emap/get-hunting-grounds
     {:hp (:hp char)
      :attack (+ (:attack_air char)
                 (:attack_earth char)
                 (:attack_fire char)
                 (:attack_water char))})))

(defn do-move-to-hunting-grounds [& [name]]
  (let [area (first (get-hunting-grounds (get-name name)))]
    (do-move area (get-name name))))
