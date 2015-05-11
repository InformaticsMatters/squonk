CREATE USER chemcentral_owner PASSWORD 'chemcentral';
CREATE USER chemcentral_user PASSWORD 'chemcentral';
CREATE USER chembl_owner PASSWORD 'chembl';
CREATE USER chembl_user PASSWORD 'chembl';
CREATE USER vendordbs_owner PASSWORD 'vendordbs';
CREATE USER vendordbs_user PASSWORD 'vendordbs';

-- migth need to do this for RDS:
-- GRANT chemcentral_owner TO postgres;
-- GRANT vendordbs_owner TO postgres;
-- GRANT chembl_owner TO postgres;

CREATE SCHEMA chemcentral_02 authorization chemcentral_owner;
CREATE SCHEMA vendordbs authorization vendordbs_owner;
CREATE SCHEMA chembl_20 authorization chembl_owner;

GRANT USAGE ON SCHEMA vendordbs TO chemcentral_owner;
GRANT SELECT, REFERENCES ON all tables IN SCHEMA vendordbs TO chemcentral_owner;
GRANT SELECT ON SCHEMA vendordbs TO chemcentral_user;
GRANT SELECT ON all tables IN SCHEMA vendordbs TO chemcentral_user;

GRANT USAGE ON SCHEMA chembl_20 TO chemcentral_owner;
GRANT SELECT, REFERENCES ON all tables IN SCHEMA chembl_20 TO chemcentral_owner;
GRANT SELECT ON SCHEMA chembl_20 TO chemcentral_user;
GRANT SELECT ON all tables IN SCHEMA chembl_20 TO chemcentral_user;

GRANT USAGE ON SCHEMA chemcentral_02 TO chemcentral_user;
GRANT SELECT ON all tables IN SCHEMA chemcentral_02 TO chemcentral_user;


