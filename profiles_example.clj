{:profiles/dev
 {:env
  {:database-url "jdbc:postgresql://localhost:5432/self-destruct-dev?user=selfdestruct&password=selfdestruct"
   :database-encryption-key "changemedbkey1"
   :reported-log-level "debug"
   :session-cookie-key "changecookiekey1"
   :enable-workers "false"
   :message-expire-minutes "1440"
   :worker-delay-seconds "3600"}}
 :profiles/test
 {:env
  {:database-url "jdbc:postgresql://localhost:5432/self-destruct-test?user=selfdestruct&password=selfdestruct"
   :database-encryption-key "changemedbkey2"
   :reported-log-level "debug"
   :session-cookie-key "changecookiekey2"
   :disable-anti-forgery "true"
   :enable-workers "false"}}
 :profiles/prod
 ;; intentionally empty to ensure builds do not rely on run time values
 {:env {}}}
