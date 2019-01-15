(ns self-destruct.message.view.index
  (:require [self-destruct.home.view.layout :as home.view.layout])
  (:require [hiccup.core :refer [html]]))


(defn message-page [message]
  (home.view.layout/page-layout
   (html
    [:div#tagline
     "create read-once, self-destructing, messages."]

    [:div.content
     [:p "This message has been deleted, please make note of it before closing this window."]
     [:p "Message:"]
     [:p (str message)]])))
