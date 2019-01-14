(ns self-destruct.message.handler
  (:require [self-destruct.config        :refer [db-url]]
            [self-destruct.message.model :as    message.model]
            [self-destruct.util.core     :as    util])
  (:require [ring.util.response :as response]
            [taoensso.timbre    :as timbre]))


(defn handle-create-message! [req]
  (let [message    (get-in req [:params :message])
        message-id (message.model/create-message! db-url {:message message})]
    (timbre/info (str "message created: " (util/uuid->str message-id)))
    ;; return the link here
    (response/redirect "/")))


(defn handle-delete-message! [req]
  (let [message-id (java.util.UUID/fromString (:message-id (:route-params req)))
        exists? (message.model/delete-message! db-url {:message-id message-id})]
    (if exists?
      (do
        (timbre/info (str "message deleted: " message-id))
        (response/redirect "/"))
      (do
        (timbre/error (str "message delete failed message id not found: " message-id))
        (response/not-found "Message not found.")))))
