CREATE TABLE users.datasets (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    time_created TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    metadata JSONB,
    loid BIGINT NOT NULL
);