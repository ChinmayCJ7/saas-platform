CREATE TABLE subscriptions (
                               id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                               tenant_id     UUID        NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                               plan          VARCHAR(20) NOT NULL CHECK (plan IN ('FREE','PRO','ENTERPRISE')),
                               billing_cycle VARCHAR(10) NOT NULL DEFAULT 'MONTHLY'
                                   CHECK (billing_cycle IN ('MONTHLY','ANNUAL')),
                               status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                                   CHECK (status IN ('ACTIVE','CANCELLED','PAST_DUE')),
                               started_at    TIMESTAMP   NOT NULL DEFAULT now(),
                               renews_at     TIMESTAMP,
                               created_at    TIMESTAMP   NOT NULL DEFAULT now()
);