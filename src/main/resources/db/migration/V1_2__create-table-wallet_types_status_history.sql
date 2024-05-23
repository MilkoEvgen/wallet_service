create table wallet_types_status_history
(
    id                      bigserial primary key,
    created_at              timestamp default now() not null,
    wallet_type_id          integer                 not null references wallet_types (id),
    changed_by_user_uid     uuid                    not null,
    changed_by_profile_type varchar(20)             not null,
    reason                  varchar(50)             not null,
    from_status             varchar(24),
    comment                 varchar(512),
    to_status               varchar(24)
);

create index wallet_types_status_history_id
    on wallet_types_status_history (id);
