(ns self-destruct.message.view.link
  (:require [self-destruct.home.view.layout :as home.view.layout])
  (:require [hiccup.core :refer [html]]))


(defn link-page [message-id]
  (let [message-link (str "/message/fetch/" message-id)]
    (home.view.layout/page-layout
     (html
      [:div.content
       [:p "Send a link to " [:strong "THIS"] " page to the recipient and " [:strong "NOT"] " the link below."]
       [:p "The message can only be viewed " [:strong "ONCE"] "."]
       [:p [:a.button {:href message-link} "click to reveal message"]]]))))
