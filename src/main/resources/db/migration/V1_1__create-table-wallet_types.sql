create table wallet_types
(
    uuid          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at    timestamp        default now() not null,
    modified_at   timestamp,
    name          varchar(32)                    not null,
    currency_code varchar(3)                     not null,
    status        varchar(18)                    not null,
    archived_at   timestamp,
    profile_type  varchar(15),
    creator       varchar(255),
    modifier      varchar(255)
);