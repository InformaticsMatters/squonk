#!/bin/sh

docker pull rabbitmq:3-management
docker pull nginx:1.13
docker pull tomcat:8.0-jre8
docker pull tomcat:7-jre8
docker pull jboss/keycloak-postgres:2.1.0.Final

docker pull informaticsmatters/rdkit_cartridge:Release_2016_03_1
docker pull informaticsmatters/rdkit:latest
docker pull informaticsmatters/rdkit:Release_2017_03_1
docker pull informaticsmatters/rdkit_java:latest
docker pull informaticsmatters/rdkit_java:Release_2017_03_1
docker pull informaticsmatters/rdkit_java_tomcat:latest
docker pull informaticsmatters/rdkit_java_tomcat:Release_2017_03_1
docker pull informaticsmatters/rdkit_pipelines:latest
docker pull informaticsmatters/rdkit_nextflow:latest
docker pull informaticsmatters/rdock:latest
docker pull informaticsmatters/rdock_nextflow:latest

docker pull abradle/standardiser
docker pull abradle/smog2016
docker pull abradle/pli
docker pull abradle/obabel

