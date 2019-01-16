(ns self-destruct.message.view.link
  (:require [self-destruct.home.view.layout :as home.view.layout])
  (:require [hiccup.core :refer [html]]))


(defn link-page [message-id]
  (let [message-link (str "/message/fetch/" message-id)]
    (home.view.layout/page-layout
     (html
      [:div#tagline
       "create read-once, self-destructing, messages."]

      [:div.content
       [:p "Send this link to the recipient, the message can only be viewed ONCE."]
       [:p [:a {:href message-link} "message link"]]]))))
