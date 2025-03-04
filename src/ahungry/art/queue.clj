(ns ahungry.art.queue
  (:require
   [clojure.tools.logging :as log]))

(def queue (atom {}))

(defn clear []
  (reset! queue {}))

(defn qadd [name f]
  "Add to the queue - FIFO (append right, pop left)."
  (let [k (keyword name)]
    (if-not (:fn f)
      (throw (Exception. "Please include a proper :fn in the qadd map.")))
    (swap! queue (fn [y] (conj y {k (vec (conj (or (k y) []) f))})))))

(defn qpop [name]
  "Pop off the queue - FIFO (append right, pop left)."
  (let [k (keyword name)
        res (first (k @queue))]
    (swap! queue (fn [y] (conj y {k (rest (k y))})))
    res))

(defn fpop [name]
  "Pop off the next item from the queue and execute it if possible."
  (let [{f :fn} (qpop name)]
    (when f
      (try
        (f)
        (catch Exception ex (log/error (str ex)))))))

(def do-next-action fpop)

(defn has-actions? [name]
  (let [k (keyword name)]
    (> (count (k @queue)) 0)))

(defn show []
  (log/info "CURRENT QUEUE EVENTS:")
  (map (fn [k]
         (->> (map :desc (k @queue))
              (map #(prn %))))
       (keys @queue)))
