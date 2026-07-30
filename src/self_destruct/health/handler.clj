(ns self-destruct.health.handler
  (:require [self-destruct.health.model :as health.model]
            [self-destruct.config       :as config])
  (:require [cheshire.core :as json]
            [taoensso.timbre :as timbre]))


(defn handle-health [req]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string {:healthy true})})


(defn handle-deep-health [req]
  (try
    (let [healthy? (boolean (:exists (health.model/deep-health (config/db-url))))]
      {:status (if healthy? 200 503)
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:healthy healthy?
                                    :check-type "database connection"})})
    (catch Exception e
      (timbre/error e "deep health check failed")
      {:status 503
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:healthy false
                                    :check-type "database connection"})})))
