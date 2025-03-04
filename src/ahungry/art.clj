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

(defn init [{:keys [name preferred-routine forbidden-routines] :as m}]
  (log/info "Char:" name "Routine:" preferred-routine
            "Forbidden:" forbidden-routines)
  (future
    (let [stagger (rand-int 5)]
      (log/info "Staggering for" stagger "seconds to avoid simultaneous calls.")
      (Thread/sleep (* 1000 stagger)))
    (let [char (c/fetch-char name)]
      (c/import-char! char)
      (c/progress char)
      (r/do-routine! m))))

(defn -main [& args]
  (let [opts (parse-opts args)
        preferred-routine (keyword (:prefer-routine opts))]
    (log/info "Starting up...")
    (let [my-chars
          [
           {:name "ahungry"
            :preferred-routine preferred-routine
            :forbidden-routines []}
           {:name "ahungry-min"
            :preferred-routine :mining
            :forbidden-routines [:fighting :woodcutting :crafting :recycling :equipping]}
           {:name "ahungry-nim"
            :preferred-routine :mining
            :forbidden-routines [:fighting :woodcutting :crafting :recycling :equipping]}
           {:name "ahungry-cut"
            :preferred-routine :woodcutting
            :forbidden-routines [:fighting :mining :crafting :recycling :equipping]}
           {:name "ahungry-tuc"
            :preferred-routine :woodcutting
            :forbidden-routines [:fighting :mining :crafting :recycling :equipping]}
           ]]
      (doall (map init my-chars)))))
