-- existing plaintext messages cannot be encrypted without their cleartext keying
-- material; purge unread notes before requiring an IV column
delete from message;
--;;
alter table message
add column message_iv char(32) not null;
