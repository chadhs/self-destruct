-- src/self_destruct/message/sql/message.sql
-- self-destruct message queries


-- :name create-message! :? :n
-- :doc insert a message item, returning the id, thus the ? in the name rather than ! for execute
insert into message (message)
values (:message)
returning id


-- :name delete-message! :! :n
-- :doc delete a single message by message_id
delete from message
where id = :message-id


-- :name read-message :? :n
-- :doc get a message by id
select id, message, date_created from message
where id = :message-id
