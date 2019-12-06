(ns self-destruct.message.worker
  (:require [self-destruct.config        :as config]
            [self-destruct.message.model :as message.model])
  (:require [environ.core    :as environ]
            [tea-time.core   :as tt]
            [taoensso.timbre :as timbre]))


(defn expire-and-purge-messages
  "purge unread messages after minutes of time has passed."
  []
  (let [message-expire-minutes (or (environ/env :message-expire-minutes) 1440)
        ;; default to 24 hours (1440 minutes)
        purged-messages-count (message.model/expire-and-purge-messages!
                               (config/db-url)
                               {:expire-minutes (str message-expire-minutes " minutes")})]
    (if (int? purged-messages-count)
      (if (> purged-messages-count 0)
        (timbre/info (str purged-messages-count " expired messages successfully purged from database"))
        (timbre/info "no expired messages to purge from database"))
      (timbre/error "failed to run expire-and-purge-messages"))))


(defn expire-and-purge-messages-scheduler
  "run expire-and-purge-messages on a set schedule"
  []
  (let [worker-time-seconds (Integer/valueOf (or (environ/env :worker-delay-seconds) 3600))]
    (tt/every!
     worker-time-seconds ; run every ___ seconds
     worker-time-seconds ; starting  ___ seconds from now
     (bound-fn [] (expire-and-purge-messages)))))
