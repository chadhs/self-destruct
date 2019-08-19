(ns self-destruct.core
  (:require [self-destruct.config :as config]
            [self-destruct.route  :as route])
  (:require [environ.core                   :as    environ]
            [ring.adapter.jetty             :as    jetty]
            [ring.middleware.defaults       :refer :all]
            [ring.middleware.webjars        :refer [wrap-webjars]]
            [ring.middleware.resource       :refer [wrap-resource]]
            [ring.middleware.reload         :refer [wrap-reload]]
            [ring.middleware.session.cookie :refer [cookie-store]])
  (:gen-class))


;; calls to load before main app handler
(defn init []
  (do
    ;; load logging configuration prior to app load
    (config/configure-logging)
    ;; run database migrations prior to app load
    (config/run-db-migration)))


;; define main application
(def app
  (-> route/combined-routes
      ;; wrap-defaults includes ring middleware in the correct order to provide:
      ;; csrf protection, session data, url parameters, static assets, and more
      (wrap-defaults
       (-> (if (= "true" (environ/env :secure-defaults))
             secure-site-defaults
             site-defaults)
           (assoc-in [:session :store] (cookie-store {:key config/session-cookie-key}))
           (assoc-in [:session :cookie-attrs] {:max-age 3600})
           (assoc :proxy true)))
      ;; set path for webjar assets
      wrap-webjars))


;; main application entry point
(defn -main
  ([]     (-main 8000))
  ([port] (do
            (timbre/info "running init tasks")
            (init)
            (timbre/info (str "starting the app on port " port "..."))
            (jetty/run-jetty app
                             {:port (Integer. port)}))))


;; development mode main application entry point
(defn -dev-main
  ([]     (-dev-main 8000))
  ([port] (do
            (timbre/info "DEV: running init tasks")
            (init)
            (timbre/info (str "DEV: starting the app on port " port "..."))
            (jetty/run-jetty (wrap-reload #'app)
                             {:port (Integer. port)}))))
