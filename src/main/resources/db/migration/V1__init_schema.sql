create table todos (
    id bigserial primary key,
    title varchar(255) not null,
    description TEXT,
    completed boolean not null default false,
    created_at timestamp without time zone not null default now()
);