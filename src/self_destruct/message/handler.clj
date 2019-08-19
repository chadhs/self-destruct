(ns self-destruct.message.handler
  (:require [self-destruct.config             :refer [db-url]]
            [self-destruct.message.model      :as    message.model]
            [self-destruct.util.core          :as    util]
            [self-destruct.message.view.index :as    message.view.index]
            [self-destruct.message.view.link  :as    message.view.link])
  (:require [ring.util.response :as response]
            [taoensso.timbre    :as timbre]))


(defn handle-create-message! [req]
  (let [message    (get-in req [:params :message])
        message-id (message.model/create-message! (db-url) {:message message})]
    (do
      (timbre/info (str "message created: " (util/uuid->str message-id)))
      (response/redirect (str "/message/link/" (util/uuid->str message-id))))))


(defn handle-delete-message! [req]
  (let [message-id (java.util.UUID/fromString (:message-id (:route-params req)))
        exists? (message.model/delete-message! (db-url) {:message-id message-id})]
    (if exists?
      (do
        (timbre/info (str "message deleted: " message-id))
        (response/redirect "/"))
      (do
        (timbre/error (str "message delete failed message id not found: " message-id))
        (response/not-found "Message not found.")))))


(defn handle-view-message-link [req]
  (let [message-id (java.util.UUID/fromString (:message-id (:route-params req)))]
    (message.view.link/link-page message-id)))


(defn handle-fetch-message [req]
  (let [message-id (java.util.UUID/fromString (:message-id (:route-params req)))
        message    (message.model/read-message (db-url) {:message-id message-id})
        deleted?   (message.model/delete-message! (db-url) {:message-id message-id})]
    (if (and message deleted?)
      (do
        (timbre/info (str "message accessed: " message-id))
        (timbre/info (str "message deleted: " message-id))
        (message.view.index/message-page (message :message)))
      (do
        (timbre/error (str "failed to fetch message: " message-id))
        (message.view.index/message-page "Message not found.")))))
