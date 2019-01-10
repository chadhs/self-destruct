(ns self-destruct.home.view.index
  (:require [self-destruct.home.view.layout :as home.view.layout])
  (:require [hiccup.core :refer [html]]))


(defn home-page []
  (home.view.layout/page-layout
   (html


    [:div#tagline
     "coming soon..."]

    [:p.content
     "run your own self destucting note service"]
    )))
