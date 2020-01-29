-- add table to hold the job status data

CREATE TABLE users.jobstatus (
    id              SERIAL PRIMARY KEY,
    owner_id        INT NOT NULL,
    uuid            TEXT NOT NULL,
    status          TEXT NOT NULL,
    total_count     INT NOT NULL DEFAULT 0,
    processed_count INT NOT NULL DEFAULT 0,
    error_count     INT NOT NULL DEFAULT 0,
    started         TIMESTAMP DEFAULT NOW(),
    completed       TIMESTAMP,
    definition      JSONB NOT NULL,
    events          TEXT[],
    CONSTRAINT uq_jobstatus_uuid UNIQUE (uuid),
    CONSTRAINT fk_jobstatus2users FOREIGN KEY (owner_id) REFERENCES users.users (id) ON DELETE CASCADE
);

CREATE INDEX idx_js_owner_id ON users.jobstatus (owner_id);
