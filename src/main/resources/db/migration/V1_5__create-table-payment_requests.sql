create table payment_requests
(
    uuid             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at       timestamp        default now() not null,
    expired_at  timestamp        default now() not null,
    profile_uid      uuid                           not null,
    owner_wallet_uid uuid                           not null
        references wallets (uuid),
    amount           numeric(10, 2)   default 0.0   not null,
    fee              numeric(10, 2)   default 0.0   not null,
    type             text                           not null,
    status           varchar
);
