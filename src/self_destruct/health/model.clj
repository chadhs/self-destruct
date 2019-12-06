(ns self-destruct.health.model
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "self_destruct/health/sql/health.sql")
