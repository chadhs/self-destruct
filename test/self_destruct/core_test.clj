(ns self-destruct.core-test
  (:require [self-destruct.core            :refer :all]
            [self-destruct.message.handler :as    message.handler])
  (:require [clojure.test      :refer :all]
            [ring.mock.request :as    mock]
            [buddy.core.nonce  :as    nonce]))


(deftest test-static-app-routes
  (testing "main route"
    (let [response (app (mock/request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "health route"
    (let [response (app (mock/request :get "/health"))]
      (is (= 200 (:status response)))))

  (testing "home route"
    (let [response (app (mock/request :get "/home"))]
      (is (= 200 (:status response))))))


(deftest test-encryption-helpers
  (let [message-iv        (nonce/random-bytes 16)
        encryption-key    "changemedbkey1"
        message           "test message"
        encrypted-message (message.handler/encrypt-message
                           {:message        message
                            :iv             message-iv
                            :encryption-key encryption-key})
        decrypted-message (message.handler/decrypt-message
                           {:message        encrypted-message
                            :iv             message-iv
                            :encryption-key encryption-key})]
    (testing "encrypt message"
      (is (not= message encrypted-message)))

    (testing "decrypt message"
      (is (= message decrypted-message)))))
