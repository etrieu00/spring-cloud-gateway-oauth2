CREATE TABLE ${flyway:defaultSchema}.app_user
(
    id            BIGSERIAL PRIMARY KEY,
    uuid          UUID UNIQUE  NOT NULL,
    user_email    VARCHAR(255) NOT NULL,
    user_password VARCHAR(64)  NOT NULL,
    user_roles    VARCHAR(255) NOT NULL,
    first_name    VARCHAR(255) NOT NULL,
    last_name     VARCHAR(255) NOT NULL,
    ufc           UUID         NOT NULL,
    ulm           UUID         NOT NULL,
    dtc           TIMESTAMP    NOT NULL,
    dtm           TIMESTAMP    NOT NULL
);