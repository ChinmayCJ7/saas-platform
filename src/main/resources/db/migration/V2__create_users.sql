CREATE TABLE users (
                       id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id     UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       email         VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       display_name  VARCHAR(100) NOT NULL,
                       role          VARCHAR(20)  NOT NULL DEFAULT 'MEMBER'
                           CHECK (role IN ('OWNER','ADMIN','MEMBER')),
                       is_active     BOOLEAN      NOT NULL DEFAULT true,
                       created_at    TIMESTAMP    NOT NULL DEFAULT now(),
                       updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
                       UNIQUE (tenant_id, email)
);