create table transactions
(
    uuid                     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at               timestamp        default now() not null,
    modified_at              timestamp,
    linked_transaction       uuid,
    profile_uid              uuid                           not null,
    wallet_uid               uuid                           not null
        references wallets (uuid),
    wallet_name              varchar(32)                    not null,
    balance_operation_amount numeric          default 0.0   not null,
    raw_amount               numeric          default 0.0   not null,
    fee                      numeric          default 0.0   not null,
    amount_in_usd            numeric          default 0.0   not null,
    type                     varchar(32)                    not null,
    state                    varchar(32)                    not null,
    payment_request_uid      uuid                           not null
        references payment_requests (uuid) on delete cascade,
    currency_code            varchar(3)                     not null,
    refund_fee               bigint           default 0     not null
);
