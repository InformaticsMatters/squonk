#!/bin/bash  

psql --username "$POSTGRES_USER" --command "CREATE USER squonk WITH PASSWORD '${POSTGRES_SQUONK_PASS:-squonk}';"
psql --username "$POSTGRES_USER" --command "CREATE USER keycloak WITH PASSWORD '${POSTGRES_KEYCLOAK_PASS:-squonk}';"
psql --username "$POSTGRES_USER" --command "CREATE USER xwiki WITH PASSWORD '${POSTGRES_XWIKI_PASS:-squonk}';"
createdb --username "$POSTGRES_USER" -O squonk squonk
createdb --username "$POSTGRES_USER" -O keycloak keycloak
createdb --username "$POSTGRES_USER" -O xwiki xwiki
psql --username "$POSTGRES_USER" -d squonk --command "CREATE SCHEMA users AUTHORIZATION squonk;"

