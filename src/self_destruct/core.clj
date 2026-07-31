(ns self-destruct.core
  (:require [self-destruct.config :as config]
            [self-destruct.route  :as route]
            [self-destruct.worker :as worker])
  (:require [clojure.string                 :as    str]
            [clojure.tools.cli              :refer [parse-opts]]
            [environ.core                   :as    environ]
            [ring.adapter.jetty             :as    jetty]
            [ring.middleware.defaults       :refer :all]
            [ring.middleware.webjars        :refer [wrap-webjars]]
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
           ;; allow tests to disable csrf; remains enabled for normal app use
           (assoc-in [:security :anti-forgery]
                     (not= "true" (environ/env :disable-anti-forgery)))
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


(defn- handle-cli-options
  "Handle --help/--migrate. Returns an exit code when the process should
  stop, or nil when the HTTP server should start."
  [{:keys [options summary errors]}]
  (cond
    errors
    (do
      (timbre/error errors)
      (timbre/info summary)
      1)

    (:help options)
    (do
      (timbre/info summary)
      0)

    (:migrate options)
    (do
      (timbre/info "running database migrations...")
      (config/run-db-migration)
      0)

    :else
    nil))


(defn- jetty-options
  "Build Jetty options from CLI port and optional HOST env (e.g. 127.0.0.1)."
  [port]
  (let [host (environ/env :host)
        opts {:port (Integer/valueOf port)}]
    (if (and host (not (str/blank? host)))
      (assoc opts :host host)
      opts)))


;; main application entry point
(defn -main [& args]
  (let [parsed (parse-opts args cli-options)
        port   (get-in parsed [:options :port])]
    ;; configure logging for all entrypoints (help/migrate/server)
    (config/configure-logging)
    (if-let [exit-code (handle-cli-options parsed)]
      (System/exit exit-code)
      (do
        (timbre/info "running init tasks")
        ;; workers only; logging already configured above
        (worker/launch-workers)
        (timbre/info (str "starting the app on port " port
                          (when-let [host (environ/env :host)]
                            (str " host " host))
                          "..."))
        (jetty/run-jetty app (jetty-options port))))))


;; development mode main application entry point
(defn -dev-main [& args]
  (let [parsed (parse-opts args cli-options)
        port   (get-in parsed [:options :port])]
    (config/configure-logging)
    ;; honor --help/--migrate in dev as well (lein run defaults to -dev-main)
    (if-let [exit-code (handle-cli-options parsed)]
      (System/exit exit-code)
      (do
        (timbre/info "DEV: running init tasks")
        (worker/launch-workers)
        (timbre/info (str "DEV: starting the app on port " port "..."))
        (jetty/run-jetty (wrap-reload #'app)
                         (jetty-options port))))))
