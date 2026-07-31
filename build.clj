(ns build
  (:require [clojure.java.io :as io]
            [clojure.tools.build.api :as b]
            [garden.core :as garden]))


(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def uber-file "target/self-destruct.jar")
(def css-output "resources/public/css/style.css")


(defn clean [_]
  (b/delete {:path "target"})
  (b/delete {:path "resources/public/css"}))


(defn compile-css [_]
  (require 'self-destruct.css)
  (let [style @(resolve 'self-destruct.css/style)]
    (io/make-parents css-output)
    (spit css-output (apply garden/css {:pretty-print? true} style))
    (println "Wrote" css-output)))


(defn uber [_]
  (clean nil)
  (compile-css nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[self-destruct.core]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'self-destruct.core})
  (println "Uberjar written to" uber-file))
