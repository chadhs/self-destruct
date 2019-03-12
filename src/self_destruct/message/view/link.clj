(ns self-destruct.message.view.link
  (:require [self-destruct.home.view.layout :as home.view.layout])
  (:require [hiccup.core :refer [html]]))


(defn link-page [message-id]
  (let [message-link (str "/message/fetch/" message-id)]
    (home.view.layout/page-layout
     (html
      [:div.content
       [:p "Send a link to THIS page to the recipient and NOT the link below."]
       [:p "The message can only be viewed ONCE."]
       [:p [:a {:href message-link} "message link"]]]))))
