#!/bin/bash

docker pull informaticsmatters/squonk-groovy:1.0.0

docker pull rabbitmq:3-management
docker pull nginx:1.13
docker pull jboss/keycloak:4.8.3.Final

docker pull informaticsmatters/rdkit_cartridge:Release_2017_09_2
docker pull informaticsmatters/rdkit:latest
docker pull informaticsmatters/rdkit_pipelines:latest
docker pull informaticsmatters/rdkit_nextflow:latest
docker pull informaticsmatters/rdock:latest
docker pull informaticsmatters/nextflow-docker:0.30.2

docker pull abradle/standardiser
docker pull abradle/smog2016
docker pull abradle/pli
docker pull abradle/obabel
docker pull informaticsmatters/pli:latest
docker pull informaticsmatters/smog:latest
