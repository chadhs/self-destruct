(ns self-destruct.core
  (:require [self-destruct.config :as config]
            [self-destruct.route  :as route]
            [self-destruct.worker :as worker])
  (:require [clojure.tools.cli              :refer [parse-opts]]
            [environ.core                   :as    environ]
            [ring.adapter.jetty             :as    jetty]
            [ring.middleware.defaults       :refer :all]
            [ring.middleware.webjars        :refer [wrap-webjars]]
            [ring.middleware.resource       :refer [wrap-resource]]
            [ring.middleware.reload         :refer [wrap-reload]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [taoensso.timbre                :as    timbre])
  (:gen-class))


;; calls to load before main app handler
(defn init []
  (do
    ;; load logging configuration prior to app load
    (config/configure-logging)
    ;; kick off worker processes
    (worker/launch-workers)))


;; define main application
(def app
  (-> route/combined-routes
      ;; wrap-defaults includes ring middleware in the correct order to provide:
      ;; csrf protection, session data, url parameters, static assets, and more
      (wrap-defaults
       (-> (if (= "true" (environ/env :secure-defaults))
             secure-site-defaults
             site-defaults)
           ;; (assoc-in [:security :anti-forgery] false)
           (assoc-in [:session :store] (cookie-store {:key (config/session-cookie-key)}))
           (assoc-in [:session :cookie-attrs] {:max-age 3600})
           (assoc :proxy true)))
      ;; set path for webjar assets
      wrap-webjars))


;; command line options and validation
(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 8000
    ;; first parse our option
    :parse-fn #(Integer/parseInt %)
    ;; then validate our option
    :validate [#(< 0 % 65536) "Must be a number between 0 and 65536"]]
   ;; defaults to nil
   ["-m" "--migrate"]
   ;; defaults to nil
   ["-h" "--help"]])


;; main application entry point
(defn -main [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)
        port   (:port options)]
    (cond
      errors
      (do
        (timbre/error errors)
        (timbre/info summary)
        (System/exit 1))

      (:help options)
      (do
        (timbre/info summary)
        (System/exit 0))

      (:migrate options)
      (do
        (timbre/info "running database migrations...")
        (config/run-db-migration)
        (System/exit 0))

      :else
      (do
        (timbre/info "running init tasks")
        (init)
        (timbre/info (str "starting the app on port " port "..."))
        (jetty/run-jetty app
                         {:port (Integer/valueOf port)})))))


;; development mode main application entry point
(defn -dev-main [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)
        port   (:port options)]
    (do
      (timbre/info "DEV: running init tasks")
      (init)
      (timbre/info (str "DEV: starting the app on port " port "..."))
      (jetty/run-jetty (wrap-reload #'app)
                       {:port (Integer/valueOf port)}))))
