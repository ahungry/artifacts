(ns ahungry.art.repo
  (:require
   [clojure.tools.logging :as log]
   [ahungry.art.util :as util]
   [clojure.java.jdbc :as j]
   [clojure.string]
   [clojure.walk]
   [cheshire.core :refer :all]
   [clj-http.client :as client])
  (:use [slingshot.slingshot :only [throw+ try+]]))

;; https://github.com/clojure/java.jdbc
(def db
  {
   :connection-uri "jdbc:sqlite:artifacts.db?journal_mode=MEMORY&synchronous=OFF&journal_size_limit=5000000"
   })

(defn get-token []
  (-> (slurp ".token") (.trim)))

(defn sdk [method path & [opts]]
  (let [url (str "https://api.artifactsmmo.com" path)
        req (merge {:method method
                    :url url
                    :accept :json
                    :content-type "application/json"
                    :headers {:authorization (str "Bearer " (get-token))}
                    :as :json
                    :coerce :always
                    } opts)]
    (log/debug "HTTP request: " req)
    ;; (log/debug "HTTP request to: " url)
    (try+
     (let [res (-> (client/request req) :body)]
       ;; (log/debug "The response was: " res)
       res)
     (catch [:status 499] {:keys [body]}
       (log/warn "Need to wait before calling: " body) body)
     (catch [:status 598] {:keys [body]}
       (log/warn "Bad status code: " body) body)
     (catch Object {:keys [body]}
       (log/warn "Uncaught error: " body) body))))

(defn kw->route [action]
  (clojure.string/replace (name action) #"-" "/"))

(defn sdk-for [char-name]
  (fn [action & [body]]
    (log/debug "Called with: " action)
    (sdk :post (str "/my/" char-name "/action/" (kw->route action)) {:body (generate-string body)})))
