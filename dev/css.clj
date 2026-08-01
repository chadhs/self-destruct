(ns css
  "Compile Garden stylesheets to resources/public/css/style.css."
  (:require [clojure.java.io :as io]
            [garden.core :as garden]))


(def output-path "resources/public/css/style.css")


(defn compile-once!
  []
  (require 'self-destruct.css)
  (let [style @(resolve 'self-destruct.css/style)]
    (io/make-parents output-path)
    (spit output-path (apply garden/css {:pretty-print? true} style))
    (println "Wrote" output-path)))


(defn watch!
  "Recompile CSS when the stylesheet source changes."
  []
  (println "Watching Garden CSS (Ctrl-C to stop)...")
  (loop [last-mtime 0]
    (let [source (io/file "src/self_destruct/css.clj")
          mtime (.lastModified source)]
      (when (> mtime last-mtime)
        (try
          (require 'self-destruct.css :reload)
          (compile-once!)
          (catch Exception e
            (println "CSS compile failed:" (.getMessage e)))))
      (Thread/sleep 1000)
      (recur mtime))))


(defn -main
  [& args]
  (case (first args)
    "once" (compile-once!)
    "watch" (watch!)
    nil (compile-once!)
    (do
      (println "Usage: clj -M:css [once|watch]")
      (System/exit 1))))
