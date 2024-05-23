create table payment_requests
(
    uuid                     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at               timestamp        default now() not null,
    modified_at              timestamp,
    profile_uid              uuid                           not null,
    wallet_uid               uuid                           not null
        references wallets (uuid),
    amountgross              numeric          default 0.0   not null,
    fee                      numeric          default 0.0   not null,
    status                   varchar,
    percentage               numeric          default 0.0   not null,
    fixed_amount             numeric          default 0.0   not null,
    option                   varchar(3)                     not null,
    scale                    integer                        not null,
    comment                  varchar(256),
    provider_transaction_uid uuid,
    provider_transaction_id  varchar(255),
    payment_method_id        bigint
);
