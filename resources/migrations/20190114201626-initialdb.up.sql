create extension if not exists "uuid-ossp";
--;;
create table if not exists message (
id uuid primary key default uuid_generate_v4(),
message text not null,
date_created timestamptz not null default now()
);
