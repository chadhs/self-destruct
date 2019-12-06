(ns self-destruct.test.helper.db
  (:require [hugsql.core :as hugsql]))


(hugsql/def-db-fns "self_destruct/test/helper/sql/helper.sql")
(ns self-destruct.test.helper.db)
