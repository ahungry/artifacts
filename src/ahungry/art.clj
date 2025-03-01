(ns ahungry.art
  (:require
   [clojure.tools.logging :as log]
   [clojure.pprint]
   [clojure.string]
   [ahungry.art.routine :as r]
   [ahungry.art.entity.char :as c])
  (:gen-class))

(defn parse-opts
  "Turn a seq of strings into kw based map."
  [xs]
  (reduce
   (fn [acc [k v]] (conj acc {(try (read-string k) (catch Exception _ k))
                              (try (read-string v) (catch Exception _ v))}))
   {}
   (partition 2 xs)))

(defn -main [& args]
  (let [opts (parse-opts args)
        preferred-routine (keyword (:prefer-routine opts))]
    (log/info "Starting up...")
    (log/info "Fetching latest char data")
    (let [char (c/fetch-char "ahungry")]
      (c/import-char! char)
      (c/progress char))
    (log/info "Routine:" preferred-routine)
    (reset! r/prefer-routine preferred-routine)
    ;; (c/reset! prefer-routine :woodcutting)
    (future (r/do-routine! "ahungry"))))
