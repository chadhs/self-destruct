(ns self-destruct.env
  "Load a local .env file into JVM system properties for environ.

  Existing process environment variables always win. Values from .env are
  applied only when the corresponding env var is unset/blank — so production
  and CI (which export vars before starting Java) are unchanged, while local
  `clj` commands pick up `.env` automatically."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))


(defn- keywordize
  [k]
  (-> (name k)
      str/lower-case
      (str/replace #"[_\\.]" "-")
      keyword))


(defn- parse-dotenv
  "Parse KEY=VAL lines. Ignores blanks and # comments. Splits on the first =."
  [contents]
  (into []
        (keep (fn [line]
                (let [line (str/trim line)]
                  (when (and (not (str/blank? line))
                             (not (str/starts-with? line "#"))
                             (str/includes? line "="))
                    (let [idx (str/index-of line "=")
                          k   (str/trim (subs line 0 idx))
                          v   (str/trim (subs line (inc idx)))]
                      (when (re-matches #"[A-Za-z_][A-Za-z0-9_]*" k)
                        [k v])))))
              (str/split-lines contents))))


(defn load-dotenv!
  "Load `.env` from the process working directory (or `file` if given).

  - Skips keys already present in the process environment
  - Sets JVM system properties so environ can see them
  - If environ is already initialized, merges missing keys into `environ.core/env`

  Returns the parsed pairs (possibly empty), or nil when no file exists."
  ([]
   (load-dotenv! (io/file ".env")))
  ([file]
   (let [f (io/file file)]
     (when (.isFile f)
       (let [pairs (parse-dotenv (slurp f))]
         (doseq [[k v] pairs]
           (when (str/blank? (System/getenv k))
             (System/setProperty k v)))
         (when-let [environ-ns (find-ns 'environ.core)]
           (when-let [env-var (ns-resolve environ-ns 'env)]
             (alter-var-root
              env-var
              (fn [current]
                (reduce (fn [m [k v]]
                          (let [kw (keywordize k)]
                            (if (str/blank? (str (get m kw)))
                              (assoc m kw v)
                              m)))
                        current
                        pairs)))))
         pairs)))))


;; Load before other app namespaces require environ (see self-destruct.config).
(load-dotenv!)
