(ns self-destruct.db-integration-test
  (:require [self-destruct.core             :refer :all]
            [self-destruct.test.helper.core :as    test.helper])
  (:require [clojure.test      :refer :all]
            [ring.mock.request :as    mock]))


;; ensure a clean db state for each test requiring db integration
(use-fixtures :each test.helper/test-db-reset)


(deftest test-db-app-routes
  (testing "deep-health route"
    (let [response (app (mock/request :get "/deep-health"))]
      (is (= 200 (:status response))))))


(deftest test-message-handlers
  (testing "message create route"
    (let [create-response (test.helper/create-test-message)]
      (is (= 302 (:status create-response)))
      (is (not= nil (:message-id create-response)))))

  (testing "message delete route"
    (let [create-response (test.helper/create-test-message)
          delete-response (app
                           (mock/request
                            :post (str "/message/delete/" (:message-id create-response))))]
      (is (= 302 (:status delete-response)))))

  (testing "message link route"
    (let [create-response (test.helper/create-test-message)
          link-response   (app
                           (mock/request
                            :get (str "/message/link/" (:message-id create-response))))]
      (is (= 200 (:status link-response)))))

  (testing "message fetch route"
    (let [create-response (test.helper/create-test-message)
          fetch-response  (app
                           (mock/request
                            :get (str "/message/fetch/" (:message-id create-response))))]
      (is (= 200 (:status fetch-response)))))

  (testing "call message fetch route twice on same id"
    (let [create-response (test.helper/create-test-message)
          fetch-response  (app
                           (mock/request
                            :get (str "/message/fetch/" (:message-id create-response))))
          fetch-response2 (app
                           (mock/request
                            :get (str "/message/fetch/" (:message-id create-response))))]
      (is (= 404 (:status fetch-response2))))))
