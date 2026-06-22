create table if not exists chat_history_turns (
    id bigserial primary key,
    principal_name varchar(128) not null,
    conversation_id varchar(128) not null,
    role varchar(32) not null,
    content text not null,
    created_at timestamp with time zone not null default now()
);

create index if not exists idx_chat_history_turns_principal_conversation_created
    on chat_history_turns (principal_name, conversation_id, created_at desc, id desc);