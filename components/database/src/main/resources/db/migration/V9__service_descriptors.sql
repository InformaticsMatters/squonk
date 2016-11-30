CREATE TABLE users.service_descriptor_sets (
    id              SERIAL PRIMARY KEY,
    base_url        TEXT NOT NULL,
    health_url      TEXT,
    status          CHAR(1) NOT NULL,
    created         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated         TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_base_url UNIQUE (base_url)
);

CREATE TABLE users.service_descriptors (
    id              SERIAL PRIMARY KEY,
    set_id          INTEGER NOT NULL,
    sd_id           TEXT NOT NULL,
    status          CHAR(1) NOT NULL,
    sd_json         JSONB NOT NULL,
    created         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated         TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_sd_id UNIQUE (set_id, sd_id),
    CONSTRAINT fk_sds2sdsets FOREIGN KEY (set_id) REFERENCES users.service_descriptor_sets (id) ON DELETE CASCADE
);


GRANT SELECT, INSERT, UPDATE, DELETE ON users.service_descriptor_sets TO squonk;
GRANT SELECT, INSERT, UPDATE, DELETE ON users.service_descriptors TO squonk;

GRANT USAGE ON SEQUENCE users.service_descriptor_sets_id_seq TO squonk;
GRANT USAGE ON SEQUENCE users.service_descriptors_id_seq TO squonk;