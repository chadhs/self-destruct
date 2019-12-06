(ns self-destruct.test.helper.core
  (:require [self-destruct.core           :refer :all]
            [self-destruct.test.helper.db :as    test.helper.db]
            [self-destruct.config         :as    config])
  (:require [ring.mock.request :as mock]))


(defn test-db-reset
  "run a function, resetting the test db before and after running it"
  [test-function]
  (do
    (test.helper.db/test-clear-db! (config/db-url))
    (test-function)
    (test.helper.db/test-clear-db! (config/db-url))))


(defn create-test-message
  "create a test message returning a map containing the http-status-code and message-id"
  []
  (let [create-response (app (-> (mock/request :post "/message/create")
                                 (mock/body {:message "test message"})))
        status          (:status create-response)
        message-id      (-> create-response
                            (get-in [:headers "Location"])
                            (clojure.string/split #"/")
                            last)]
    {:status     status
     :message-id message-id}))
