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
            [taoensso.timbre    :as timbre]))


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


;; handlers
(defn handle-create-message! [req]
  (let [referer           (get-in req [:headers "referer"])
        message           (get-in req [:params :message])
        message-iv        (nonce/random-bytes 16)
        message-iv-hex    (codecs/bytes->hex message-iv)
        encrypted-message (encrypt-message {:message message
                                            :encryption-key (config/db-encryption-key)
                                            :iv message-iv})
        message-id        (message.model/create-message!
                           (config/db-url) {:message encrypted-message
                                            :message-iv message-iv-hex})
        ;; we want to log a unique id, but not the entire id to avoid data leakage
        message-log-id    (-> (util/uuid->str message-id)
                              bc-hash/md5
                              codecs/bytes->hex)
        proxy-ip-chain    (get-in req [:headers "x-forwarded-for"])
        client-ip         (if proxy-ip-chain
                            (-> proxy-ip-chain
                                (clojure.string/split #",")
                                first)
                            "localhost")]
    (if message-id
      (do
        (timbre/info
         (str "message created: " message-log-id " by: " client-ip))
        ;; we use the referer to ensure relative links target the proxy, not the cluster ip
        (response/redirect (str referer "message/link/" (util/uuid->str message-id))))
      (do
        (timbre/error (str "message creation failed... by: " client-ip))
        {:status 500
         :headers {"Content-Type" "application/json"}
         :body "{\"error\": \"message creation failed...\"}"}))))


(defn handle-delete-message! [req]
  (let [referer    (get-in req [:headers "referer"])
        message-id (java.util.UUID/fromString (:message-id (:route-params req)))
        ;; we want to log a unique id, but not the entire id to avoid data leakage
        message-log-id    (-> (str message-id)
                              bc-hash/md5
                              codecs/bytes->hex)
        exists?    (message.model/delete-message!
                    (config/db-url) {:message-id message-id})]
    (if exists?
      (do
        (timbre/info (str "message deleted: " message-log-id))
        (response/redirect (str referer "/")))
      (do
        (timbre/error (str "message delete failed message id not found: " message-log-id))
        (response/not-found "Message not found.")))))


(defn handle-view-message-link [req]
  (let [message-id (java.util.UUID/fromString (:message-id (:route-params req)))]
    (message.view.link/link-page message-id)))


(defn handle-fetch-message [req]
  (let [message-id        (java.util.UUID/fromString (:message-id (:route-params req)))
        ;; we want to log a unique id, but not the entire id to avoid data leakage
        message-log-id    (-> (str message-id)
                              bc-hash/md5
                              codecs/bytes->hex)
        message           (message.model/read-message (config/db-url) {:message-id message-id})
        message-text      (when message (message :message))
        deleted?          (message.model/delete-message! (config/db-url) {:message-id message-id})
        message-iv        (when message (codecs/hex->bytes (message :message_iv)))
        decrypted-message (when message (decrypt-message
                                         {:message message-text
                                          :encryption-key (config/db-encryption-key)
                                          :iv message-iv}))
        proxy-ip-chain    (get-in req [:headers "x-forwarded-for"])
        client-ip         (if proxy-ip-chain
                            (-> proxy-ip-chain
                                (clojure.string/split #",")
                                first)
                            "localhost")]
    (if (and message deleted?)
      (do
        (timbre/info (str "message accessed: " message-log-id " by: " client-ip))
        (timbre/info (str "message deleted: " message-log-id))
        (message.view.index/message-page decrypted-message))
      (do
        (timbre/error (str "failed to fetch message: " message-log-id " by: " client-ip))
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body (message.view.index/message-page "Message not found.")}))))
