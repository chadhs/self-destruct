{:profiles/dev
 {:env
  {:database-url "jdbc:postgresql://localhost/self-destruct-dev"
   :reported-log-level "debug"
   :session-cookie-key "changecookiekey1"}}
 :profiles/test
 {:env
  {:database-url "jdbc:postgresql://localhost/self-destruct-test"
   :reported-log-level "debug"
   :session-cookie-key "changecookiekey2"}}}
