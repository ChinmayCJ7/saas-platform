CREATE TABLE tasks (
                       id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id   UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       project_id  UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                       title       VARCHAR(200) NOT NULL,
                       description TEXT,
                       status      VARCHAR(20)  NOT NULL DEFAULT 'TODO'
                           CHECK (status IN ('TODO','IN_PROGRESS','DONE')),
                       priority    VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM'
                           CHECK (priority IN ('LOW','MEDIUM','HIGH')),
                       assignee_id UUID         REFERENCES users(id) ON DELETE SET NULL,
                       due_date    DATE,
                       created_by  UUID         NOT NULL REFERENCES users(id),
                       created_at  TIMESTAMP    NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);