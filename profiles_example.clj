{:profiles/dev
 {:env
  {:database-url "jdbc:postgresql://localhost/self-destruct-dev"
   :database-encryption-key "changemedbkey1"
   :reported-log-level "debug"
   :session-cookie-key "changecookiekey1"}}
 :profiles/test
 {:env
  {:database-url "jdbc:postgresql://localhost/self-destruct-test"
   :database-encryption-key "changemedbkey2"
   :reported-log-level "debug"
   :session-cookie-key "changecookiekey2"}}
 :profiles/prod
 ;; intentionally empty to ensure builds do not rely on run time values
 {:env {}}}
