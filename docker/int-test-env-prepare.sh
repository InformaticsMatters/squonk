#!/bin/bash

docker-compose rm -vf
docker-compose build
docker-compose up -d

