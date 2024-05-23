create table merchant_fee_rules
(
    id                   bigserial primary key,
    created_at           timestamp  default now()                    not null,
    modified_at          timestamp,
    transaction_type     varchar(20)                                 not null,
    percentage           numeric    default 0.0                      not null,
    fixed_amount         numeric    default 0.0                      not null,
    option               varchar(3),
    archived_at          timestamp,
    wallet_uid           uuid
        references wallets (uuid) on delete cascade,
    is_all_wallets       boolean    default false,
    wallet_type_id       integer    default '-1'::integer            not null,
    payment_method_id    integer    default '-1'::integer            not null,
    amount_from          integer,
    amount_to            integer,
    highest_priority     boolean    default false,
    fee_currency         varchar(4) default 'USD'::character varying not null,
    fee_rule_type        varchar(24),
    start_date           timestamp,
    end_date             timestamp,
    status               varchar(10),
    creator_profile_uid  uuid,
    modifier_profile_uid uuid
);

INSERT INTO merchant_fee_rules (transaction_type, percentage, status)
VALUES ('top_up', 1, 'ACTIVE'),
       ('withdraw', 0.1, 'ACTIVE'),
       ('transfer', 0.2, 'ACTIVE');
