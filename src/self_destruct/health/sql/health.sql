-- src/self_destruct/health/sql/health.sql
-- self-destruct health queries


-- :name deep-health :? :n
-- :doc verify db access
select exists (
  select 1
  from information_schema.tables
  where table_schema = 'public'
  and table_name = 'message'
  )
