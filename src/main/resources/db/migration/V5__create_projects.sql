CREATE TABLE projects (
                          id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                          tenant_id   UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                          name        VARCHAR(150) NOT NULL,
                          description TEXT,
                          status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                              CHECK (status IN ('ACTIVE','ARCHIVED','COMPLETED')),
                          created_by  UUID         NOT NULL REFERENCES users(id),
                          created_at  TIMESTAMP    NOT NULL DEFAULT now(),
                          updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);