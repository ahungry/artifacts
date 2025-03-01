(ns ahungry.art
  (:require
   [clojure.tools.logging :as log]
   [clojure.pprint]
   [clojure.string]
   [ahungry.art.entity.monster :as mon]
   [ahungry.art.entity.map :as emap]
   [ahungry.art.entity.char :as c])
  (:gen-class))

(defn -main [& args]
  (log/info "Starting up...")
  (future (c/do-actions! "ahungry")))
