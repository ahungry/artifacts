(ns user
  (:require
   [clojure.main]
   [clojure.tools.logging :as log]
   [ahungry.art :as art]))

(prn "Greetings from clj")

(defn go []
  (apply require clojure.main/repl-requires))

(go)
