(ns self-destruct.db-integration-test
  (:require [self-destruct.core             :refer :all]
            [self-destruct.test.helper.core :as    test.helper]
            [self-destruct.message.model    :as    message.model]
            [self-destruct.message.worker   :as    message.worker]
            [self-destruct.config           :as    config])
  (:require [clojure.test      :refer :all]
            [clojure.java.jdbc :as    jdbc]
            [ring.mock.request :as    mock]
            [cheshire.core     :as    json]))


;; ensure a clean db state for each test requiring db integration
(use-fixtures :each test.helper/test-db-reset)


(deftest test-db-app-routes
  (testing "deep-health route"
    (let [response (app (mock/request :get "/deep-health"))
          body     (json/parse-string (:body response) true)]
      (is (= 200 (:status response)))
      (is (true? (:healthy body))))))


(deftest test-message-handlers
  (testing "message create route"
    (let [create-response (test.helper/create-test-message)]
      (is (= 302 (:status create-response)))
      (is (not= nil (:message-id create-response)))
      (is (re-matches #"[0-9a-fA-F-]{36}" (:message-id create-response)))))

  (testing "message delete route"
    (let [create-response (test.helper/create-test-message)
          delete-response (app
                           (mock/request
                            :post (str "/message/delete/" (:message-id create-response))))]
      (is (= 302 (:status delete-response)))
      (is (nil? (message.model/read-message
                 (config/db-url)
                 {:message-id (java.util.UUID/fromString (:message-id create-response))})))))

  (testing "message link route"
    (let [create-response (test.helper/create-test-message)
          link-response   (app
                           (mock/request
                            :get (str "/message/link/" (:message-id create-response))))]
      (is (= 200 (:status link-response)))))

  (testing "message fetch route returns content and deletes message"
    (let [create-response (test.helper/create-test-message)
          message-id      (java.util.UUID/fromString (:message-id create-response))
          fetch-response  (app
                           (mock/request
                            :get (str "/message/fetch/" (:message-id create-response))))]
      (is (= 200 (:status fetch-response)))
      (is (re-find #"test message" (:body fetch-response)))
      (is (nil? (message.model/read-message
                 (config/db-url)
                 {:message-id message-id})))))

  (testing "call message fetch route twice on same id"
    (let [create-response (test.helper/create-test-message)
          fetch-response  (app
                           (mock/request
                            :get (str "/message/fetch/" (:message-id create-response))))
          fetch-response2 (app
                           (mock/request
                            :get (str "/message/fetch/" (:message-id create-response))))]
      (is (= 200 (:status fetch-response)))
      (is (= 404 (:status fetch-response2)))))

  (testing "messages are stored encrypted"
    (let [create-response (test.helper/create-test-message)
          stored          (message.model/read-message
                           (config/db-url)
                           {:message-id (java.util.UUID/fromString (:message-id create-response))})]
      (is (some? stored))
      (is (not= "test message" (:message stored)))
      (is (string? (:message_iv stored)))
      (is (= 32 (count (:message_iv stored))))))

  (testing "expired unread messages are purged"
    (let [create-response (test.helper/create-test-message)
          message-id      (java.util.UUID/fromString (:message-id create-response))]
      (jdbc/execute! (config/db-url)
                     ["update message set date_created = now() - interval '2 days' where id = ?"
                      message-id])
      (message.worker/expire-and-purge-messages)
      (is (nil? (message.model/read-message
                 (config/db-url)
                 {:message-id message-id}))))))
