(ns self-destruct.util.core)

;; general
(defn uuid->str
  "return the plain string value of a given uuid."
  [uuid]
  (str (uuid :id)))


(defn hugsqluuid->javauuid
  "return the java.util.UUID/fromString uuid format from a hugsql uuid map format."
  [uuid]
  (uuid :id))
