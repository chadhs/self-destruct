(ns dev
  "Development entrypoint: optional CSS watch + reloadable Ring server."
  (:require [css]
            [self-destruct.core :as core]))


(defn -main
  [& args]
  (future
    (try
      (css/watch!)
      (catch Exception e
        (binding [*out* *err*]
          (println "CSS watch stopped:" (.getMessage e))))))
  (apply core/-dev-main args))
