# Squonk to Minishift

## Deployment
The deployment consists of a number of templates and stages:

-   squonk-secrets
-   squonk-infra
-   squonk-app

## Initialize project

Logged in as a cluster admin (e.g. system:admin) Do these:
 
    $ oc new-project squonk
    $ oc adm policy add-role-to-user edit developer
    $ oc adm policy add-scc-to-group anyuid system:authenticated
    $ oc adm policy add-cluster-role-to-user cluster-admin -z default
    
The fist creates the squonk project.
The second gives our developer user access to the squonk project 
The third allows containers to be run as any user, not an OpenShift assigned user id.
The fourth adds cluster admin role to the service account.
We hope to reduce the need for the elevated privs used is the third and fourth commands.

If running on an environment where persistent volumes need to be manually provisioned then setup waht is 
needed (e.g. NFS exports) and then execute the appropriate squonk-pv-*.yaml template for your system. e.g.

    $ oc process -f squonk-pv-nfs.yaml | oc create -f -

Once done log in as a normal user e.g. developer.

### Create database

The database and users have to be created. This might be in a pre-existing database that is in a different
project (e.g openrisknet-infra or squonk-infra). To create this database we use the squonk-db-init.yaml 
template which creates the database, schema, users and permissions that are needed by Squonk. The template
also creates a secret in the squonk project that contains the details that Squonk needs to connect to the
database.

Run this template in the project that contains the PostgreSQL database as a user who has the necessary
privileges for that project and to write secrets into the squonk project (e.g. admin).
 
    $ oc process -f squonk-db-init.yaml | oc create -f -
    
If squonk is not running in the `squonk` project then specify the `-p SQUONK_NAMESPACE=xxxxx` parameter
to specify the project.

To check that this has worked look at the logs of the `squonk-database-creator-????` pod in the project from
 which this was run, and check that there is a secret named `squonk-database-credentials` in the squonk project.

### Secrets

Secrets need to be deployed before any other object.

    $ oc process -f squonk-secrets.yaml | oc create -f -
    
### Infrastructure
This is responsible for the operating infrastructure, which
consists of `postgres` and `rabbitmq`.

Then, deploy using:

    $ oc process -f squonk-infra.yaml | oc create -f -

### Application
>   The Application should be dependent on the infrastructure so, in theory,
    there's no need to wait for Infrastructure before you deploy these
    services.

Deploy using:

    $ oc process -f squonk-app.yaml | oc create -f -

## TODOs

-   Avoid running chemservices image as Openshift assigned user. (priority=low)
-   Split Chemservices up into its individual services and deploy independently. 
    Only rdkit-search will require access to Postgres and RabbitMQ. (priority=medium)
