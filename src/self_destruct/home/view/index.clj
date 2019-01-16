(ns self-destruct.home.view.index
  (:require [self-destruct.home.view.layout :as home.view.layout])
  (:require [hiccup.core            :refer [html]]
            [ring.util.anti-forgery :as    anti-forgery]))


(defn new-message []
  (html
   [:form
    {:method "POST" :action "/message/create"}
    (anti-forgery/anti-forgery-field)
    [:textarea
     {:name :message
      :rows 16
      :cols 72
      :placeholder "Message"
      :autofocus true}]
    [:br]
    [:button
     {:type :submit}
     "Create"]
    [:button
     {:type :reset}
     "Clear"]
    ]))


(defn home-page []
  (home.view.layout/page-layout
   (html
    [:div#tagline
     "create read-once, self-destructing, messages."]

    [:div.content
     [:h2 "Create a new message"]
     (new-message)])))
