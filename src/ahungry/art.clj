(ns ahungry.art
  (:require
   [clojure.tools.logging :as log]
   [clojure.pprint]
   [clojure.string]
   [ahungry.art.entity.monster :as m])
  (:gen-class))

(defn -main [& args]
  (log/info (m/get-monsters))
  (log/info "Hello world"))
