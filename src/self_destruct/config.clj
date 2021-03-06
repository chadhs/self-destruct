(ns self-destruct.config
  (:require [environ.core                               :as environ]
            [migratus.core                              :as migratus]
            [taoensso.timbre                            :as timbre]
            [taoensso.timbre.appenders.3rd-party.sentry :as sentry]))


;; database config
(def db-url
  (environ/env :database-url))


;; migrations
(def db-migration-config
  {:store         :database
   :migration-dir "migrations"
   :db            db-url})

(defn run-db-migration []
  ;; apply pending migrations
  (migratus/migrate db-migration-config))


;; session cookie security config
(def session-cookie-key
  (environ/env :session-cookie-key))


;; logging config
(def reported-log-level
  (keyword (or (environ/env :reported-log-level) "warn")))


(def log-appender
  (or (environ/env :log-appender) "println"))


(defn configure-logging []
  (timbre/merge-config!
   {:appenders
    (cond
      (= "println" log-appender) {:println {:output-fn :inherit}}
      (= "sentry" log-appender)  {:sentry-appender
                                  (merge
                                   (sentry/sentry-appender (environ/env :sentry-dsn))
                                   {:min-level reported-log-level})})}))
