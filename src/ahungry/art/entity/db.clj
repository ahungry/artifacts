(ns ahungry.art.entity.db
  (:require
   [ahungry.art.util :as util]
   [clojure.java.jdbc :as j]
   [clojure.string]
   [clojure.walk]))

;; https://github.com/clojure/java.jdbc
;; "jdbc:sqlite:%s?journal_mode=WAL&synchronous=OFF&journal_size_limit=500"
;; (j/execute! db-con "PRAGMA synchronous = OFF")
;; (j/query db-con "PRAGMA journal_mode = MEMORY")
(def db
  {
   ;; :dbtype "sqlite"
   ;; :dbname "override.db"
   ;; :journal_mode "MEMORY"
   ;; :synchronous "OFF"

   :connection-uri "jdbc:sqlite:artifacts.db?journal_mode=MEMORY&synchronous=OFF&journal_size_limit=5000000"
   ;; :connection-uri "jdbc:sqlite::memory:?journal_mode=MEMORY&synchronous=OFF&journal_size_limit=5000000"
   })
