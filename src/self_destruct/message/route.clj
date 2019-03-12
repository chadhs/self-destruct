(ns self-destruct.message.route
  (:require [self-destruct.message.handler :as    message.handler])
  (:require [compojure.core                :refer [defroutes ANY GET POST PUT DELETE]]))


(defroutes message-routes
  (POST "/message/create"             [] message.handler/handle-create-message!)
  (POST "/message/delete/:message-id" [] message.handler/handle-delete-message!)
  (GET  "/message/link/:message-id"   [] message.handler/handle-view-message-link)
  (GET  "/message/fetch/:message-id"  [] message.handler/handle-fetch-message))
