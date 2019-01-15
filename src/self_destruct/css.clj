(ns self-destruct.css
  (:require [garden.def :refer [defstyles]]))


(def colors
  {:background   "#fff"
   :text         "#333"
   :tagline      "#333"
   :link         "blue"
   :button       "blue"
   :button-hover "green"
   })


(defstyles style

  [:html
   {:background-color (colors :background)
    :font-size        "16px"}]

  [:body
   :button
   :input
   :select
   :textarea
   {:color       (colors :text)
    :font-family "Helvetica Neue, Helvetica, sans-serif"}]

  [:body {:background-color (colors :background)}]

  [:a.site-title
   :a:hover.site-title
   :a:visited.site-title
   {:text-decoration "none"
    :color           (colors :text)}]

  [:h1
   {:font-size   "4rem"
    :font-weight "normal"
    :text-align  "center"}]

  [:#tagline
   {:font-size   "2rem"
    :font-weight "normal"
    :text-align  "center"
    :color       (colors :tagline)}]

  [:a:link
   {:color           (colors :link)
    :text-decoration "none" }]

  [:a:hover
   {:color           (colors :link)
    :text-decoration "underline"}]

  [:a:visited
   {:color (colors :link)}]

  [:.container
   {:background-color (colors :background)}]

  [:.content
   {:font-size     "1.4rem"
    :text-align    "center"
    :background-color (colors :background)
    :color         (colors :text)
    :margin-left  "1rem"
    :margin-right "1rem"}]

  )
