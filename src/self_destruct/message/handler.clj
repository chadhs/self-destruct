(ns self-destruct.message.handler
  (:require [self-destruct.config             :as config]
            [self-destruct.message.model      :as message.model]
            [self-destruct.util.core          :as util]
            [self-destruct.message.view.index :as message.view.index]
            [self-destruct.message.view.link  :as message.view.link])
  (:require [ring.util.response :as response]
            [buddy.core.crypto  :as crypto]
            [buddy.core.codecs  :as codecs]
            [buddy.core.nonce   :as nonce]
            [buddy.core.hash    :as hash]
            [taoensso.timbre    :as timbre]
            [clojure.string     :as str]))


;; helpers
(defn encrypt-message
  "takes a message, encryption key, and initialization vector;
  encrypts and hex encodes message for easy database storage"
  [{:keys [message encryption-key iv]}]
  (let [encryption-key (hash/sha256 encryption-key)]
    (-> message
        codecs/to-bytes
        (crypto/encrypt encryption-key iv {:algorithm :aes128-cbc-hmac-sha256})
        codecs/bytes->hex)))


(defn decrypt-message
  "takes a message, encryption key, and initialization vector;
  hex decodes and decrypts stored message"
  [{:keys [message encryption-key iv]}]
  (let [encryption-key (hash/sha256 encryption-key)]
    (-> message
        codecs/hex->bytes
        (crypto/decrypt encryption-key iv {:algorithm :aes128-cbc-hmac-sha256})
        codecs/bytes->str)))


(defn- message-log-id
  "Hash a message id for logging so full secret ids are not written to logs."
  [message-id]
  (-> (str message-id)
      hash/md5
      codecs/bytes->hex))


(defn- client-ip
  [req]
  (if-let [proxy-ip-chain (get-in req [:headers "x-forwarded-for"])]
    (-> proxy-ip-chain
        (str/split #",")
        first
        str/trim)
    (or (:remote-addr req) "unknown")))


(defn- parse-message-id
  "Parse a route message id into a UUID. Returns nil when invalid."
  [message-id-str]
  (try
    (java.util.UUID/fromString message-id-str)
    (catch Exception _
      nil)))


(defn- blank-message?
  [message]
  (or (nil? message)
      (str/blank? message)))


;; handlers
(defn handle-create-message! [req]
  (let [message (get-in req [:params :message])
        ip      (client-ip req)]
    (if (blank-message? message)
      {:status 400
       :headers {"Content-Type" "text/plain"}
       :body "Message is required."}
      (let [message-iv        (nonce/random-bytes 16)
            message-iv-hex    (codecs/bytes->hex message-iv)
            encrypted-message (encrypt-message {:message message
                                                :encryption-key (config/db-encryption-key)
                                                :iv message-iv})
            message-id        (message.model/create-message!
                               (config/db-url) {:message encrypted-message
                                                :message-iv message-iv-hex})
            log-id            (when message-id (message-log-id (util/uuid->str message-id)))]
        (if message-id
          (do
            (timbre/info (str "message created: " log-id " by: " ip))
            (response/redirect (str "/message/link/" (util/uuid->str message-id))))
          (do
            (timbre/error (str "message creation failed... by: " ip))
            {:status 500
             :headers {"Content-Type" "text/plain"}
             :body "Message creation failed."}))))))


(defn handle-delete-message! [req]
  (if-let [message-id (parse-message-id (:message-id (:route-params req)))]
    (let [log-id  (message-log-id message-id)
          deleted (message.model/delete-message!
                   (config/db-url) {:message-id message-id})]
      (if (and (int? deleted) (pos? deleted))
        (do
          (timbre/info (str "message deleted: " log-id))
          (response/redirect "/"))
        (do
          (timbre/error (str "message delete failed message id not found: " log-id))
          (response/not-found "Message not found."))))
    (response/not-found "Message not found.")))


(defn handle-view-message-link [req]
  (if-let [message-id (parse-message-id (:message-id (:route-params req)))]
    (message.view.link/link-page message-id)
    (response/not-found "Message not found.")))


(defn handle-fetch-message [req]
  (if-let [message-id (parse-message-id (:message-id (:route-params req)))]
    (let [log-id            (message-log-id message-id)
          ip                (client-ip req)
          ;; Atomic consume: delete and return content in one statement so
          ;; concurrent fetches cannot both reveal the same message.
          message           (message.model/fetch-message!
                             (config/db-url) {:message-id message-id})
          message-text      (when message (:message message))
          message-iv        (when message (codecs/hex->bytes (:message_iv message)))
          decrypted-message (when message
                              (decrypt-message
                               {:message message-text
                                :encryption-key (config/db-encryption-key)
                                :iv message-iv}))]
      (if message
        (do
          (timbre/info (str "message accessed: " log-id " by: " ip))
          (timbre/info (str "message deleted: " log-id))
          (message.view.index/message-page decrypted-message))
        (do
          (timbre/error (str "failed to fetch message: " log-id " by: " ip))
          {:status 404
           :headers {"Content-Type" "text/html"}
           :body (message.view.index/message-page "Message not found.")})))
    {:status 404
     :headers {"Content-Type" "text/html"}
     :body (message.view.index/message-page "Message not found.")}))
