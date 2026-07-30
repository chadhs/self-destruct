-- src/self_destruct/message/sql/message.sql
-- self-destruct message queries


-- :name create-message! :<! :1
-- :doc insert a message item, returning the id
insert into message (message, message_iv)
values (:message, :message-iv)
returning id


-- :name delete-message! :! :n
-- :doc delete a single message by message_id
delete from message
where id = :message-id


-- :name read-message :? :1
-- :doc get a message by id
select id, message, message_iv, date_created from message
where id = :message-id


-- :name fetch-message! :<! :1
-- :doc atomically delete and return a message by id (exactly-once consume)
delete from message
where id = :message-id
returning id, message, message_iv, date_created


-- :name expire-and-purge-messages! :! :n
-- :doc remove messages from database that have not been read in :expire_minutes
delete from message
where date_created < now() - :expire-minutes::interval
