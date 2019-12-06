(ns self-destruct.health.route
  (:require [self-destruct.health.handler :as health.handler])
  (:require [compojure.core :refer [defroutes GET]]))


(defroutes health-routes
  (GET  "/health"      [] health.handler/handle-health)
  (GET  "/deep-health" [] health.handler/handle-deep-health))
