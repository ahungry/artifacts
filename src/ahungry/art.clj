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
  (log/info "Fetching latest char data")
  (let [char (c/fetch-char "ahungry")]
       (c/import-char! char)
       (c/progress char))
  (reset! c/prefer-routine :fighting)
  ;; (c/reset! prefer-routine :woodcutting)
  (future (c/do-actions! "ahungry")))
