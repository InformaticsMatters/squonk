#!/bin/sh

cd images
docker build -t squonk/xwiki -f Dockerfile-xwiki .
docker build -t squonk/keycloak -f Dockerfile-keycloak .

# finally move back to the original dir
cd ..


