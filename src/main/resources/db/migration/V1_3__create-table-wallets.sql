create table wallets
(
    uuid           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at     timestamp        default now() not null,
    modified_at    timestamp,
    name           varchar(32)                    not null,
    wallet_type_id integer                        not null references wallet_types (id),
    profile_uid    uuid                           not null,
    status         varchar(30)                    not null,
    balance        numeric          default 0.0   not null,
    archived_at    timestamp
);


create index wallets_id_uidx
    on wallets (uuid);
