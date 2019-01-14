(ns self-destruct.message.model
  (:require [hugsql.core :as hugsql]))


(hugsql/def-db-fns "self-destruct/message/sql/message.sql")
