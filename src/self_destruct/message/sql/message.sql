-- src/self_destruct/message/sql/message.sql
-- self-destruct message queries


-- :name create-message! :? :n
-- :doc insert a message item, returning the id, thus the ? in the name rather than ! for execute
insert into message (message, message_iv)
values (:message, :message-iv)
returning id


-- :name delete-message! :! :n
-- :doc delete a single message by message_id
delete from message
where id = :message-id


-- :name read-message :? :n
-- :doc get a message by id
select id, message, message_iv, date_created from message
where id = :message-id


-- :name expire-and-purge-messages! :! :n
-- :doc remove messages from database that have not been read in :expire_minutes
delete from message
where date_created < now() - :expire-minutes::interval
