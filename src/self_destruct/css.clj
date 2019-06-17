(ns self-destruct.css
  (:require [garden.def :refer [defstyles]]))


(def colors
  {:background   "#fff"
   :text         "#333"
   :tagline      "#333"
   :link         "blue"
   :button       "#157FFB"
   :button-hover "#106CD6"
   :button-text  "#fff"})


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

  [:textarea
   {:font-size "1rem"}]

  [:button
   {:background-color (colors :button)
    :color (colors :button-text)
    :border-radius "4px"
    :border "none"
    :padding "0.5rem 1.0rem"
    :margin "0.5rem"
    :font-size "1.2rem"
    :text-align "center"
    :text-decoration "none"
    :display "inline-block"}]

  [:.button
   {:background-color (colors :button)
    :color (colors :button-text)
    :border-radius "4px"
    :border "none"
    :padding "0.5rem 1.0rem"
    :margin "0.5rem"
    :font-size "1.2rem"
    :text-align "center"
    :text-decoration "none"
    :display "inline-block"}]

  [:button:hover
   {:background-color (colors :button-hover)
    :color (colors :button-text)}]

  [:.button:hover
   {:background-color (colors :button-hover)
    :color (colors :button-text)}]

  [:a.button
   :a:link.site-title
   :a:hover.button
   :a:visited.button
   {:text-decoration "none"
    :color (colors :button-text)}])
