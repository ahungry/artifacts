(ns ahungry.art.entity.char
  (:require
   [ahungry.art.repo :refer [db sdk sdk-for]]
   [ahungry.art.entity.map :as emap]
   [clojure.tools.logging :as log]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clj-http.client :as client]))

(defn get-char [name]
  (-> (sdk :get (str "/characters/" name)) :data))

(defn do-rest [name]
  ((sdk-for name) :rest))

(defn do-fight [name]
  ((sdk-for name) :fight))

(defn do-move [name {:keys [x y]}]
  ((sdk-for name) :move {:x x :y y}))

(defn get-hunting-grounds [name]
  (let [char (get-char name)]
    (emap/get-hunting-grounds
     {:hp (:hp char)
      :attack 4})))

(defn do-move-to-hunting-grounds [name]
  (let [area (first (get-hunting-grounds name))]
    (do-move name area)))
