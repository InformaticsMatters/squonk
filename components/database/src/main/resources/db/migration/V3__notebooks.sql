-- add tables to hold the notebook and variable definitions

CREATE TABLE users.nb_definition (
    id              SERIAL PRIMARY KEY,
    owner_id        INT NOT NULL,
    name            VARCHAR(50) NOT NULL,
    description     VARCHAR(400) NOT NULL,
    created         TIMESTAMP NOT NULL,
    updated         TIMESTAMP NOT NULL,
    CONSTRAINT nbdef_uq_name UNIQUE (owner_id, name),
    CONSTRAINT nbdef2users FOREIGN KEY (owner_id) REFERENCES users.users (id)
);

CREATE TABLE users.nb_version (
    id              SERIAL PRIMARY KEY,
    notebook_id     INT NOT NULL,
    parent_id       INT,
    owner_id        INT NOT NULL,
    type            CHAR(1) CHECK (type IN ('S', 'W')),
    label           VARCHAR(50),
    description     VARCHAR(400),
    nb_definition   JSONB,
    CONSTRAINT nbver_uq_label UNIQUE (notebook_id, label),
    CONSTRAINT nbver2nbdef FOREIGN KEY (notebook_id) REFERENCES users.nb_definition (id) ON DELETE CASCADE,
    CONSTRAINT nbver2parent FOREIGN KEY (parent_id) REFERENCES users.nb_version (id) ON DELETE CASCADE,
    CONSTRAINT nbver2users FOREIGN KEY (owner_id) REFERENCES users.users (id)
);

CREATE TABLE users.nb_variable (
    id              SERIAL PRIMARY KEY,
    source_id       INT NOT NULL,
    cell_id         INT NOT NULL,
    var_name       VARCHAR(50) NOT NULL,
    var_key         VARCHAR(20) NOT NULL,
    val_txt         TEXT,
    val_blob        BYTEA,
    CONSTRAINT nbvar_uq UNIQUE (source_id, var_name, var_key),
    CONSTRAINT nbvar2source FOREIGN KEY (source_id) REFERENCES users.nb_version (id) ON DELETE CASCADE
);

-- use EXTERNAL rather than EXTENDED as the data should already be compressed
ALTER TABLE users.nb_variable ALTER val_blob SET STORAGE EXTERNAL;


GRANT SELECT, INSERT, UPDATE, DELETE ON users.nb_definition TO squonk;
GRANT SELECT, INSERT, UPDATE, DELETE ON users.nb_version TO squonk;
GRANT SELECT, INSERT, UPDATE, DELETE ON users.nb_variable TO squonk;

GRANT USAGE ON SEQUENCE users.nb_definition_id_seq TO squonk;
GRANT USAGE ON SEQUENCE users.nb_version_id_seq TO squonk;
GRANT USAGE ON SEQUENCE users.nb_variable_id_seq TO squonk;