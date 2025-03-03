(ns ahungry.art.queue
  (:require
   [clojure.tools.logging :as log]))

(def queue (atom {}))

(defn qadd [name f]
  "Add to the queue - FIFO (append right, pop left)."
  (let [k (keyword name)]
    (if-not (clojure.test/function? f)
      (throw (Exception. "Only functions may be added to the queue!")))
    (swap! queue (fn [y] (conj y {k (vec (conj (or (k y) []) f))})))))

(defn qpop [name]
  "Pop off the queue - FIFO (append right, pop left)."
  (let [k (keyword name)
        res (first (k @queue))]
    (swap! queue (fn [y] (conj y {k (rest (k y))})))
    res))

(defn fpop [name]
  "Pop off the next item from the queue and execute it if possible."
  (let [f (qpop name)]
    (when f
      (try
        (f)
        (catch Exception ex (log/error (str ex)))))))

(defn has? [name]
  (let [k (keyword name)]
    (> (count (k @queue)) 0)))
