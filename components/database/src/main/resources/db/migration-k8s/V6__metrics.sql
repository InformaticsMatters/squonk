CREATE TABLE users.metrics_tokens_costs_history (
    id              SERIAL PRIMARY KEY,
    key             TEXT NOT NULL,
    cost            NUMERIC(12,6) NOT NULL,
    created         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE users.metrics_tokens_costs (
    id              SERIAL PRIMARY KEY,
    key             TEXT NOT NULL,
    version         INT NOT NULL,
    CONSTRAINT uq_tc_key UNIQUE (key),
    CONSTRAINT fk_tc2tch FOREIGN KEY (version) REFERENCES users.metrics_tokens_costs_history (id)
);

CREATE TABLE users.metrics_tokens_usage (
    id              SERIAL PRIMARY KEY,
    job_uuid        TEXT NOT NULL,
    key             TEXT NOT NULL,
    units           INT NOT NULL,
    tokens          NUMERIC(9,3),
    version         INT,
    created         TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_mt2js FOREIGN KEY (job_uuid) REFERENCES users.jobstatus (uuid) ON DELETE CASCADE,
    CONSTRAINT fk_mt2tch FOREIGN KEY (version) REFERENCES users.metrics_tokens_costs_history (id)
);
