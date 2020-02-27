CREATE TABLE users.users (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    active          INT NOT NULL DEFAULT 1,
    time_created    TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username)
);

-- legacy table - will be deleted
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

CREATE TABLE users.demo_files (
      id SERIAL PRIMARY KEY,
      name TEXT NOT NULL,
      time_created TIMESTAMP NOT NULL DEFAULT NOW(),
      last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
      size INTEGER,
      metadata JSONB,
      loid BIGINT NOT NULL
);

-- legacy table - will be deleted
CREATE TABLE users.jobs (
    id SERIAL PRIMARY KEY,
    owner_id INTEGER NOT NULL,
    job_class TEXT NOT NULL,
    jobdata JSONB NOT NULL,
    CONSTRAINT fk_jobs2users FOREIGN KEY (owner_id) REFERENCES users.users (id) ON DELETE CASCADE
);

INSERT INTO users.users (username) VALUES ('squonkuser');
INSERT INTO users.users (username) VALUES ('user1');
INSERT INTO users.users (username) VALUES ('user2');
