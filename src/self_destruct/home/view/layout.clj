(ns self-destruct.home.view.layout
  (:require [hiccup.page :refer [html5]]))


(defn page-layout [content]
  (html5
   {:lang :en}
   [:head
    [:title "Self-Destruct"]
    [:meta {:name :viewport
            :content "width=device-width, initial-scale=1.0"}]
    [:link {:href "/assets/font-awesome/css/all.min.css"
            :rel :stylesheet
            :type "text/css"}]
    [:link {:href "/css/style.css"
            :rel :stylesheet
            :type "text/css"}]]
   [:body
    [:div.container
     [:h1 [:a.site-title {:href "/"} "Self-Destruct"]]
     content]]))
