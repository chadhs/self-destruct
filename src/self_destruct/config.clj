(ns self-destruct.config
  (:require [environ.core                               :as environ]
            [migratus.core                              :as migratus]
            [taoensso.timbre                            :as timbre]
            [taoensso.timbre.appenders.3rd-party.sentry :as sentry]))


;; database config
(defn db-url []
  (environ/env :database-url))


;; migrations
(defn db-migration-config []
  {:store         :database
   :migration-dir "migrations"
   :db            (db-url)})

(defn run-db-migration []
  ;; apply pending migrations
  (migratus/migrate (db-migration-config)))


;; session cookie security config
(defn session-cookie-key []
  (environ/env :session-cookie-key))


;; logging config
(defn reported-log-level []
  (keyword (or (environ/env :reported-log-level) "warn")))


(defn log-appender []
  (or (environ/env :log-appender) "println"))


(defn configure-logging []
  (timbre/merge-config!
   {:appenders
    (cond
      (= "println" (log-appender)) {:println {:output-fn :inherit}}
      (= "sentry" (log-appender))  {:sentry-appender
                                    (merge
                                     (sentry/sentry-appender (environ/env :sentry-dsn))
                                     {:min-level (reported-log-level)})})}))
