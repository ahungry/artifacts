(ns ahungry.art
  (:require
   [clojure.tools.logging :as log]
   [clojure.pprint]
   [clojure.string]
   [ahungry.art.routine :as r]
   [ahungry.art.entity.char :as c])
  (:gen-class))

(defn -main [& args]
  (log/info "Starting up...")
  (log/info "Fetching latest char data")
  (let [char (c/fetch-char "ahungry")]
       (c/import-char! char)
       (c/progress char))
  (reset! r/prefer-routine :fighting)
  ;; (c/reset! prefer-routine :woodcutting)
  (future (r/do-routine! "ahungry")))
