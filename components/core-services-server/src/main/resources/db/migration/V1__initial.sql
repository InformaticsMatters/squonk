CREATE TABLE users.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    active INT NOT NULL DEFAULT 1,
    time_created TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE users.datasets (
    id SERIAL PRIMARY KEY,
    owner_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    time_created TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    metadata JSONB,
    loid BIGINT NOT NULL,
    CONSTRAINT fk_datasets2users FOREIGN KEY (owner_id) REFERENCES users.users (id) ON DELETE CASCADE
);

CREATE TABLE users.jobs (
    id SERIAL PRIMARY KEY,
    owner_id INTEGER NOT NULL,
    job_class TEXT NOT NULL,
    jobdata JSONB NOT NULL,
    CONSTRAINT fk_jobs2users FOREIGN KEY (owner_id) REFERENCES users.users (id) ON DELETE CASCADE
);

INSERT INTO users.users (username) VALUES ('testuser');