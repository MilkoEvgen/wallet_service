create table withdrawal_requests
(
    uuid                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at          timestamp        default now() not null,
    provider            varchar,
    token               varchar,
    payment_request_uid uuid                           not null
        references payment_requests (uuid) on delete cascade
);
