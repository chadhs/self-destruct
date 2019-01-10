(ns self-destruct.css
  (:require [garden.def :refer [defstyles]]))


(def colors
  {:feature-green  "#018574"
   :text           "#c6cddb"
   :light-grey     "#494f5c"
   :dark-grey      "#3B3E48"
   :highlight-grey "#7d828a"
   :midnight-blue  "#2c3e50"
   :alt-blue       "#2D70BD"})


(defstyles style

  [:html
   {:background-color (colors :light-grey)
    :font-size        "16px"}]

  [:body
   :button
   :input
   :select
   :textarea
   {:color       (colors :text)
    :font-family "Helvetica Neue, Helvetica, sans-serif"}]

  [:body {:background-color (colors :light-grey)}]

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
    :color       (colors :feature-green)}]

  [:.contact
   {:font-size   "2rem"
    :font-weight "normal"
    :text-align  "center"
    :margin      "1rem"}]

  [:.far
   {:vertical-align "middle"}]

  [:.signup
   {:text-align  "center"
    :margin      "1rem"}]

  [:a:link
   {:color           (colors :alt-blue)
    :text-decoration "none" }]

  [:a:hover
   {:color           (colors :alt-blue)
    :text-decoration "underline"}]

  [:a:visited
   {:color (colors :alt-blue)}]

  [:.container
   {:background-color (colors :light-grey)}]

  [:.content
   {:font-size     "1.4rem"
    :text-align    "center"
    :background-color (colors :light-grey)
    :color         (colors :text)
    :margin-left  "1rem"
    :margin-right "1rem"}]

  [:.button
   {:background-color (colors :alt-blue)
    :color (colors :text)
    :border-radius "4px"
    :border "none"
    :padding "0.5rem 1.0rem"
    :margin "0.5rem"
    :font-size "1.2rem"
    :text-align "center"
    :text-decoration "none"
    :display "inline-block"
    }]

  [:.button:hover
   {:background-color (colors :feature-green)}]

  [:a.button
   :a:hover.button
   :a:visited.button
   {:text-decoration "none"
    :color (colors :text)}]

  )
