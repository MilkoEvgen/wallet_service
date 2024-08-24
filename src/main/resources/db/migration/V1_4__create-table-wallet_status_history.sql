create table wallet_status_history
(
    uuid                      uuid,
    created_at              timestamp default now() not null,
    wallet_uid              uuid                    not null
        references wallets (uuid) on delete cascade,
    changed_by_user_uid     uuid,
    changed_by_profile_type varchar(20)             not null,
    reason                  varchar(50)             not null,
    from_status             varchar(24),
    comment                 varchar(512),
    to_status               varchar(24)
);

create index wallets_status_history_id
    on wallet_status_history (uuid);
