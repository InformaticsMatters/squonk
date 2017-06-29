====================================================================================
====================================================================================
====================== Description of containers ===================================
====================================================================================
====================================================================================

A full Squonk deployment is created from a number of Docker images.
All images can be used directly from DockerHub. Some are standard Docker provided images, others need to be built
and pushed to the DockerHub squonk area.
The deployment is manages through the docker-compose.yml file (this will migrate to a Kubernetes/OpenShift deployment). 

The following Docker containers form a Squonk runtime environment

1. RabbitMQ Message Queue
-------------------------
Container name: rabbitmq
Image source: rabbitmq:3-management
Provides message queues uses by the Squonk middle tier.
This is configured initially using the images/rabbitmq/rabbitmq-setup.sh script.

2. PostgreSQL database
----------------------
Container name: postgres
Image source: informaticsmatters/rdkit_cartridge
PostgreSQL database running RDKit cartridge (for chemsearch) based on the informaticsmatters/rdkit_cartrige image and provides:
- all the Squonk runtime data (squonk schema)
- keycloak configuration data (keycloak schema)
- xwiki content (xwiki schema)
Schemas and users are created when a new environment is set up using the contents of the images/init-postgres-db.sh file.

3. Keycloak SSO
---------------
Container name: keycloak
Image source: jboss/keycloak-postgres
This provides SSO capabilities to the portal and xwiki applications. The realm is named squonk.

4. Chemservices
---------------
Container name: chemservices
Image source: squonk/chemservices
This provides the chemistry related REST web services that are used by Squonk.
Currently services from all vendors are packed into a single image (see the TODO at the bottom of the doc).

5. Coreservices
---------------
Container name: coreservices
Image source: squonk/coreservices
This provides the middle tier Squonk services accessed by the Portal application.

6. cellexecutor
---------------
Container name: cellexecutor
Image source: squonk/cellexecutor
This orchestrates job execution, brokering execution of REST web services and Docker based job execution.

7. Portal
---------
Container name: portal
Image source: squonk/portal
This provides the front end web application. Currently this module is not in the informaticsmatters/squonk GitHub repository.
The portal application primarily interacts with the coreservices container.
Authentication/Authorization is provides by the keycloak container.

8. XWiki
--------
Container name: xwiki
Image source: squonk/xwiki
The provides the Wiki that contains the user documentation.
Authentication/Authorization is provides by the keycloak container.

9. NGinx
--------
Container name: nginx
Image source: nginx
This provides the front end reverse proxy to all services (currently portal and xwiki).
This is the only service that is public facing.




====================================================================================
====================================================================================
====================== Build Docker images and push to Docker Hub ==================
====================================================================================
====================================================================================


This builds all the required Docker images and pushes them to DockerHub
You can currently only do this if you have access to the informaticsmatters/portal GitHub repository that is not yet 
open source (it will be integrated into the open source informaticsmatters/squonk repository at some stage). 

./images-push.sh

====================================================================================
====================================================================================
====================== First time preparation of a deployment ======================
====================================================================================
====================================================================================

Your host machine must have Java, Ant, Docker and Docker-compose installed.


1. Pull Docker images
---------------------

This uses images from Docker Hub that has been done by someone using step 1, though the squonk ones can be built 
locally using step 1 above or the images-build-all.sh script.
To pull the necessary images do this:

./pull-docker-images.sh

Note: this is necessary as the Docker images used for runtime execution of services currently MUST already be 
pulled from the Docker repository and available locally on the Docker host.

2. Define environment variables
-------------------------------

You need to define a number of environment variables that contain information needed by the containers (passwords etc.).
The easiest way to to create a setenv.sh script from the setenv-default.sh template and to edit accordingly. If you do not
edit these you will be left with the default passwords that eny can find by looking at these files. The setenv.sh file is
excluded from version control so is only visible to you.
You must at least edit the PUBLIC_HOST and PRIVATE_HOST variables.
 
cp setenv-default.sh  setenv.sh    # create the file that defines the environment variables
# edit setenv.sh as needed changing passwords and docker gateway address
source setenv.sh                   # to set the environment variables


3. Setup containers
-------------------

Now, for the first time you must setup the core (non Squonk) containers e.g. postgres, keycloak, rabbitmq, xwiki

./containers-setup-core.sh         # one-off setup and configuration of the core containers that only needs doing once

Now setup the squonk related containers:

./containers-setup-app.sh


4. Start the containers
-----------------------
   
./containers-run.sh               

Monitor the containers:

docker-compose ps

Sometimes the nginx container fails to start as it has to wait too long for other services. If this happens manually start it:

docker-compose start nginx


5. Miscellaneous stuff
----------------------

All volatile data is contained in the data directory. This might grow quite large.
To backup a Squonk instance you should back up this whole /docker/deploy directory including the /docker/deploy/data directory


Depending on the size for the chemistry databases the PostgreSQL memory may need to be adjusted.
See data/pgdata/postgresql.conf
edit shared_buffers property and restart the postgres container
TODO - describe how to generate these chemistry databases.


====================================================================================
====================================================================================
====================== Update an existing deployment ===============================
====================================================================================
====================================================================================

If you make changes to the Squonk or Portal code you need to update the deployment.
Note: the Portal code is not yet public.

./images-build-services
./imges-build-portal
./containers-setup-app.sh
./containers-run.sh



====================================================================================
====================================================================================
====================== Miscellaneous stuff ===============================
====================================================================================
====================================================================================

To export the real squonk realm from Keykloak:

(Note: this probably will not work - TODO - fix this)
docker run -it --link deploy_postgres_1:postgres -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=squonk --rm -v $PWD:/tmp/json jboss/keycloak-postgres:2.1.0.Final -b 0.0.0.0 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/json/squonk.json -Dkeycloak.migration.realmName=squonk


To build a new EC2 environment:
Image: 
ubuntu/images/hvm-ssd/ubuntu-wily-15.10-amd64-server-20160204 - ami-6610390c
ubuntu/images/hvm-ssd/ubuntu-wily-15.10-amd64-server-20160405 - ami-8b9087e1

Ubuntu 15.10 image
Install openjdk-8-jdk ant jq
Install docker-engine (https://docs.docker.com/compose/install/)
Install docker compose (https://docs.docker.com/compose/install/)

pull the squonk and portal repos
build as above (remember to create/edit setenv.sh)




====================================================================================
====================================================================================
====================== TODO Items ==================================================
====================================================================================
====================================================================================

XWiki
-----
1. Provide mechanism for custom values for xwiki.authentication.validationKey adn xwiki.authentication.encryptionKey in xwiki.cfg.
2. Examine other aspects of xwiki.cfg that might need attention.
3. Upgrade XWIki to latest version. Particular attention to migrating existing content and keycloak security authentication. 

Rabbitmq
--------
1. Find better way to do initial congiguration

Keycloak
--------
1. Provide mechanism for generating custom secrets and keys, and applying to xwiki and portal containers.
2. Update docs above on how to export squonk realm configuration.

Chemservices
------------
1. Break out into separate services for each vendor rather than one uber-service to allow ChemAxon ones to be 
excluded if you don't have license file and to allow faster startup.

Postgres
--------
1. Seperate out the chemsearch from the squonk user data into 2 different databases so that the chemserch can be 
shared across multiple Squonk instances and lower the memory requirements for a Squonk instance.


