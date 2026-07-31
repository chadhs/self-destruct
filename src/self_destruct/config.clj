(ns self-destruct.config
  (:require [environ.core    :as environ]
            [migratus.core   :as migratus]
            [taoensso.timbre :as timbre])
  (:import [java.nio.charset StandardCharsets]))


;; database config
(defn db-url []
  (environ/env :database-url))

(defn db-encryption-key []
  (environ/env :database-encryption-key))


;; migrations
(defn db-migration-config []
  {:store         :database
   :migration-dir "migrations"
   ;; migratus 1.6+ requires a next.jdbc db spec map
   :db            {:jdbcUrl (db-url)}})

(defn run-db-migration []
  ;; apply pending migrations
  (migratus/migrate (db-migration-config)))


;; jetty config
(defn jetty-https-config [port]
  {:ssl?           true
   :ssl-port       (Integer/valueOf port)
   :keystore       (environ/env :keystore)
   :truststore     (environ/env :truststore)
   :key-password   "changeit"
   :trust-password "changeit"})

(defn jetty-http-config [port]
  {:port (Integer/valueOf port)})


;; session cookie security config
(defn session-cookie-key
  "Return the cookie-store key as a 16-byte array (Ring's preferred form)."
  []
  (let [key (environ/env :session-cookie-key)]
    (cond
      (bytes? key) key
      (string? key) (.getBytes ^String key StandardCharsets/UTF_8)
      :else key)))


;; logging config
(defn reported-log-level []
  (keyword (or (environ/env :reported-log-level) "warn")))


(defn log-appender []
  (or (environ/env :log-appender) "println"))


(defn- sentry-appender
  "Load the Timbre community Sentry appender on demand so println-only
  deployments do not need to initialize raven-clj at namespace load time."
  [dsn]
  (require 'taoensso.timbre.appenders.community.sentry)
  ((resolve 'taoensso.timbre.appenders.community.sentry/sentry-appender) dsn))


(defn configure-logging []
  (timbre/merge-config!
   {:appenders
    (cond
      (= "println" (log-appender)) {:println {:output-fn :inherit}}
      (= "sentry" (log-appender))  {:sentry-appender
                                    (merge
                                     (sentry-appender (environ/env :sentry-dsn))
                                     {:min-level (reported-log-level)})})}))
