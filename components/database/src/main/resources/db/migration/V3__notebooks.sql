-- add tables to hold the notebook and variable definitions

CREATE TABLE users.nb_descriptor (
    id              SERIAL PRIMARY KEY,
    owner_id        INT NOT NULL,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(400) NOT NULL,
    created         TIMESTAMP NOT NULL,
    updated         TIMESTAMP NOT NULL,
    CONSTRAINT nbdes_uq_name UNIQUE (owner_id, name),
    CONSTRAINT nbdes2users FOREIGN KEY (owner_id) REFERENCES users.users (id)
);

CREATE TABLE users.nb_version (
    id              SERIAL PRIMARY KEY,
    notebook_id     INT NOT NULL,
    parent_id       INT CHECK (parent_id != id),
    owner_id        INT NOT NULL,
    created         TIMESTAMP NOT NULL,
    updated         TIMESTAMP NOT NULL,
    type            CHAR(1) CHECK (type IN ('S', 'E')),
    label           VARCHAR(50),
    description     VARCHAR(400),
    nb_definition   JSONB,
    CONSTRAINT nbver_uq_label UNIQUE (notebook_id, label),
    CONSTRAINT nbver2nbdef FOREIGN KEY (notebook_id) REFERENCES users.nb_descriptor (id) ON DELETE CASCADE,
    CONSTRAINT nbver2parent FOREIGN KEY (parent_id) REFERENCES users.nb_version (id) ON DELETE CASCADE,
    CONSTRAINT nbver2users FOREIGN KEY (owner_id) REFERENCES users.users (id)
);

CREATE TABLE users.nb_variable (
    id              SERIAL PRIMARY KEY,
    source_id       INT NOT NULL,
    cell_id         INT NOT NULL,
    var_name        VARCHAR(50) NOT NULL,
    var_key         VARCHAR(20) NOT NULL,
    created         TIMESTAMP NOT NULL,
    updated         TIMESTAMP NOT NULL,
    val_text        TEXT,
    val_blob        BYTEA,
    CONSTRAINT nbvar_uq UNIQUE (source_id, cell_id, var_name, var_key),
    CONSTRAINT nbvar2source FOREIGN KEY (source_id) REFERENCES users.nb_version (id) ON DELETE CASCADE
);

-- use EXTERNAL rather than EXTENDED as the data should already be compressed
ALTER TABLE users.nb_variable ALTER val_blob SET STORAGE EXTERNAL;


GRANT SELECT, INSERT, UPDATE, DELETE ON users.nb_descriptor TO squonk;
GRANT SELECT, INSERT, UPDATE, DELETE ON users.nb_version TO squonk;
GRANT SELECT, INSERT, UPDATE, DELETE ON users.nb_variable TO squonk;

GRANT USAGE ON SEQUENCE users.nb_descriptor_id_seq TO squonk;
GRANT USAGE ON SEQUENCE users.nb_version_id_seq TO squonk;
GRANT USAGE ON SEQUENCE users.nb_variable_id_seq TO squonk;