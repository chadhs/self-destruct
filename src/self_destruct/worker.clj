(ns self-destruct.worker
  (:require [self-destruct.message.worker :as message.worker])
  (:require [environ.core    :as environ]
            [tea-time.core   :as tt]
            [taoensso.timbre :as timbre]))


(defn launch-workers
  "launch all application worker processes"
  []
  (let [enable-workers-env (environ/env :enable-workers)
        enable-workers?    (when enable-workers-env (Boolean/valueOf enable-workers-env))]
    ;; run workers by default, false must be set explicitly
    (when (or (true? enable-workers?) (nil? enable-workers?))
      ;; start tea timer thread pool
      (tt/start!)
      ;; run worker schedules
      (message.worker/expire-and-purge-messages-scheduler))))
