(ns self-destruct.health.handler
  (:require [self-destruct.health.model :as health.model]
            [self-destruct.config       :as config])
  (:require [cheshire.core :as json]))


(defn handle-health [req]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string {:healthy true})})


(defn handle-deep-health [req]
  (let [healthy? (get (health.model/deep-health (config/db-url)) :exists)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string {:healthy healthy?
                                  :check-type "database connection"})}))
