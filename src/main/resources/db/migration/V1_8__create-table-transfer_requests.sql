create table transfer_requests
(
    uuid                     UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at               timestamp        default now() not null,
    system_rate              varchar,
    payment_request_uid_from uuid                           not null
        references payment_requests (uuid) on delete cascade,
    recipient_uid            uuid                           not null,
    wallet_uid_to            uuid                           not null
);
