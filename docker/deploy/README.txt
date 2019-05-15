====================================================================================
====================================================================================
====================== Description of containers ===================================
====================================================================================
====================================================================================

A full Squonk deployment is created from a number of Docker images.
All images can be used directly from DockerHub. Some are standard Docker provided images, others need to be built
and pushed to the DockerHub squonk area.
The deployment is managed through the docker-compose.yml file (this will migrate to a Kubernetes/OpenShift deployment). 

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
PostgreSQL database running RDKit cartridge (for chemcentral search) based on the informaticsmatters/rdkit_cartrige image and provides:
- all the Squonk runtime data (squonk schema)
- keycloak configuration data (keycloak schema)
Schemas and users are created when a new environment is set up using the contents of the images/init-postgres-db.sh file.

3. Keycloak SSO
---------------
Container name: keycloak
Image source: jboss/keycloak-postgres
This provides SSO capabilities to the portal applications. The realm is named squonk.

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

6. CellExecutor
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

8. NGinx
--------
Container name: nginx
Image source: nginx
This provides the front end reverse proxy to all services (currently portal and keycloak).
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

A full guide for setting up on a Ubuntu host, including setting up TLS can be found [here](ubunutu-setup.md).

Your host machine must have Java, Ant, jq, Docker and Docker-compose installed.

First define the environment. Create setenv.sh by copying setenv-default.sh and editing
it as appropriate. This file defines the type of Squonk environment you want to deploy
and passwords to use etc. There are 3 types of environment you can run:
dev - for local testing and development. This uses basic authentication so does not inlcude Keycloak and NGinx
basic - for more representative setup that uses Keycloak and NGinx
site - for the full Squonk site also including the informatics matters web site
If using basic or site then you must fist create certificates for nginx:

mkdir -p images/nginx/certs/squonk.it
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout images/nginx/certs/squonk.it/privkey.pem -out images/nginx/certs/squonk.it/fullchain.pem
openssl dhparam -out images/nginx/certs/squonk.it/dhparam.pem 2048

The squonk web site is initiated with a dummy page that is fine for testing.
If instead you want a real site then place the contents in
images/nginx/sites/squonk.it/_site/
This is designed for sites created with Jekyll, but as long as the content gets put into that
directory it doesn't really matter how it is created.

Then source the setenv.sh file:

source setenv.sh

Then build the images and fire them up:

./images-build-all.sh
./containers-setup-infra.sh
./containers-setup-app.sh

====================================================================================
====================================================================================
====================== Update an existing deployment ===============================
====================================================================================
====================================================================================

If you make changes to the Squonk or Portal code you need to update the deployment.
Note: the Portal code is not yet public.

./images-build-services.sh
./containers-setup-app.sh

Alternatively if you are pulling from pre-built docker images do something like this:

```sh
./pull-docker-images.sh
/containers-setup-infra.sh
/containers-setup-app.sh
```

Remember to have sourced the setenv.sh file first.

====================================================================================
====================================================================================
====================== Miscellaneous stuff ===============================
====================================================================================
====================================================================================

All volatile data is contained in the data directory. This might grow quite large.
To backup a Squonk instance you should back up this whole /docker/deploy directory including the /docker/deploy/data directory


Depending on the size for the chemistry databases the PostgreSQL memory may need to be adjusted.
See data/pgdata/postgresql.conf
edit shared_buffers property and restart the postgres container
TODO - describe how to generate these chemistry databases.

To export the real squonk realm from Keycloak:

docker run -it --network deploy_squonk_back\
  -e POSTGRES_PORT_5432_TCP_ADDR=postgres\
  -e POSTGRES_DATABASE=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=squonk \
  --rm -v $PWD:/tmp/json:z jboss/keycloak-postgres:2.1.0.Final -b 0.0.0.0 \
  -Dkeycloak.migration.action=export\
  -Dkeycloak.migration.provider=singleFile\
  -Dkeycloak.migration.file=/tmp/json/squonk.json\
  -Dkeycloak.migration.realmName=squonk

The realm definition will be found in the file squonk.json


====================================================================================
====================================================================================
====================== TODO Items ==================================================
====================================================================================
====================================================================================

Rabbitmq
--------
1. Find better way to do initial configuration

Keycloak
--------
1. Provide mechanism for generating custom secrets and keys, and applying portal containers.
2. Update docs above on how to export squonk realm configuration.

Chemservices
------------
1. Break out into separate services for each vendor rather than one uber-service to allow ChemAxon ones to be 
excluded if you don't have license file and to allow faster startup.



