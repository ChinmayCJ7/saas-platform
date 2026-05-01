CREATE TABLE tenants (
                         id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                         name        VARCHAR(100) NOT NULL,
                         slug        VARCHAR(60)  NOT NULL UNIQUE,
                         plan        VARCHAR(20)  NOT NULL DEFAULT 'FREE'
                             CHECK (plan IN ('FREE','PRO','ENTERPRISE')),
                         plan_status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                             CHECK (plan_status IN ('ACTIVE','SUSPENDED','CANCELLED')),
                         created_at  TIMESTAMP    NOT NULL DEFAULT now(),
                         updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);