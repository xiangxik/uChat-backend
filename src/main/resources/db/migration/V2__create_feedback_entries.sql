create table if not exists feedback_entries (
    id bigserial primary key,
    message_id varchar(128) not null,
    rating integer not null,
    created_at timestamp with time zone not null default now()
);

create index if not exists idx_feedback_entries_message_created
    on feedback_entries (message_id, created_at desc, id desc);
