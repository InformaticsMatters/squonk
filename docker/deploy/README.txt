To setup:
=========

create a self-signed certificate for keycloak and put it in the keycloak dir. see:
http://keycloak.github.io/docs/userguide/keycloak-server/html/server-installation.html#d4e345

Inital config:
--------------
cp setenv-default.sh  setenv.sh    # create the file that defines the environment variables
# edit setenv.sh as needed changing passwords and docker gateway address
source setenv.sh                   # to set the environment variables

First time setup of core containers
-----------------------------------
e.g. postgres, keycloak, rabbitmq
./containers-setup-core.sh         # one-off setup and configuration of the core containers that only needs doing once


Build or update the app
-----------------------
cd images
./images-build-all.sh              # build all docker images
cd ..
./containers-setup-app.sh          # setup of the applciation containers. This will need doing whenever the code udpates
./containers-run.sh                # start the containers

Build the rdkitservices image
-----------------------------
This is in separate github project: https://github.com/InformaticsMatters/rdkit-compose
To build image, form the base dir of that project:
docker build -t squonk/rdkitserver .
Then in this directory, with service not running:
docker-compose rm -fv rdkitserver
Then carry on from the containers-setup-app.sh step.

To run:
=======

docker-compose up -d --no-recreate
or if all containers have been created
docker-compose start


To export the real configuration:
=================================
docker run -it --link deploy_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=squonk --rm -v $PWD:/tmp/json jboss/keycloak-postgres:1.9.1.Final -b 0.0.0.0 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/squonk-191.json -Dkeycloak.migration.realmName=squonk


To build a new environment:
===========================
Image: 
ubuntu/images/hvm-ssd/ubuntu-wily-15.10-amd64-server-20160204 - ami-6610390c
ubuntu/images/hvm-ssd/ubuntu-wily-15.10-amd64-server-20160405 - ami-8b9087e1

Ubuntu 15.10 image
Install openjdk-8-jdk ant jq
Install docker-engine (https://docs.docker.com/compose/install/)
Install docker compose (https://docs.docker.com/compose/install/)

pull the lac and portal repos
build as above (remember to create/edit setenv.sh)


User data for EC2 instance
==========================
#!/bin/bash
sudo -i
# general
apt-get update
apt-get install -y openjdk-8-jdk ant jq

# docker
#apt-get install -y apt-transport-https ca-certificates
echo "deb https://apt.dockerproject.org/repo ubuntu-wily main" > /etc/apt/sources.list.d/docker.list
apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
apt-get update
apt-get install -y linux-image-extra-$(uname -r)
apt-get install -y docker-engine
usermod -aG docker ubuntu

# docker-compose

curl -L https://github.com/docker/compose/releases/download/1.6.2/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
curl -L https://raw.githubusercontent.com/docker/compose/$(docker-compose version --short)/contrib/completion/bash/docker-compose > /etc/bash_completion.d/docker-compose








