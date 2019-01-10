(ns self-destruct.home.handler
  (:require [self-destruct.home.view.index :as home.view.index]))


(defn handle-home-index [req]
  (home.view.index/home-page))
