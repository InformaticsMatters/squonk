# Instructions for Squonk install using Keycloak.

This is work in progress and needs to be further streamlined.
The idea is to allow the name of the *-infra and the squonk projects to be changed
(and for them to be the same). The configuration is controlled by the contents of
the `setenv.sh` file which should be created from the `setenv-template.sh` file.

By convention we refer to the project containing the infrastructure components (PostgreSQL, RabbitMQ, Keycloak)
as the `squonk-infra` project but this can be set using the $OC_INFRA_PROJECT environment variable.

By convention we refer to the project containing the application components
(portal, cellexectuor, coreservices, chemservices-basic ...) as the `squonk` project
but this can be set using the $OC_PROJECT environment variable.
 
The process is broken into two key parts:

1.  Deploy the infrastructure needed by Squonk. This includes PostgreSQL,
    Keycloak and RabbitMQ. This might already be deployed,
    for instance, if running in an OpenRiskNet environment.
    Files for doing this are in the squonk-infra directory.
2.  Deploy the Squonk application components.
    Files for doing this are in the squonk-app directory.

Before doing this you need to do some basic setup, so let's get started.

## Setup

### Configure the Installation

Before you start you must have or create the `admin` (`$OC_ADMIN_USER`)
and the `squonk` (`$OC_USER`) accounts and give the admin user cluster-admin role.

As the `system:admin` user when on the master node:

```
oc adm policy add-cluster-role-to-user cluster-admin admin
```

Now back on the node where the installation is happening (e.g. the bastion node):

Create/edit `setenv.sh` from the supplied `setenv-template.sh` template. At the very least you
need to define `OC_MASTER_HOSTNAME`, which in minishift is likely to be something 
like `192.168.99.100`. Several other variables will also likely need to be set.

Once done, _source_ the file...

```
source setenv.sh
```

### Test logins

Ensure that you have `$OC_ADMIN` and `$OC_USER` by testing a login of each.

>   This forces entering the password, which won't be required again in your
    session therefore avoiding the need for oc passwords later in the process.

```
oc login -u $OC_ADMIN
oc login -u $OC_USER
```

### Create Keycloak image streams

As that `admin` user you must deploy the xpaas image streams to your OpenShift environment:

```
oc create -f https://raw.githubusercontent.com/openshift/openshift-ansible/master/roles/openshift_examples/files/examples/v$OC_OPENSHIFT_VERSION/xpaas-streams/jboss-image-streams.json -n openshift
```

This only needs to be done once.

### Create Certificates

Create the certificates and keystores used by Keycloak.
The certs and keystores are protected by a single password that
is specified as the `$OC_CERTS_PASSWORD` variable.

```
./certs-create.sh
```

This only needs to be done once.

### Create Projects

Create projects as the `$OC_ADMIN_USER` user:
```
oc new-project $OC_PROJECT --display-name='Squonk Applications'
oc new-project $OC_INFRA_PROJECT --display-name='Application Infrastructure'
```

If you delete the projects you will also need to manually delete the PVs that are created in the next step.

## Create Infrastructure

### Infrastructure PVs and PVCs

Create the PVs required by Postgres and RabbitMQ.

#### If using Minishift:

TODO

#### If using NFS with OpenShift: 

First create NFS exports on the node that is acting as the NFS server (probably the infrastructure node) 
for `/exports/pv-postgresql` and `/exports/pv-rabbitmq` and then define the PVs and PVCs:

```
oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT -p NFS_SERVER=$OC_NFS_SERVER -f infra-pv-nfs.yaml | oc create -n $OC_INFRA_PROJECT -f -
```

This creates PVs for the NFS mounts and binds the PVCs that RabbitMQ and PostgreSQL need. This is 'permanant' coupling
of the PVC to the PV so that this (and any data in the NFS mounts) can be retained between deployments.

Following this you should see something like this (irrelevant entries are excluded):

```
$ oc get pv,pvc
NAME                                          CAPACITY   ACCESSMODES   RECLAIMPOLICY   STATUS    CLAIM                                   STORAGECLASS   REASON    AGE
pv/pv-postgresql                              50Gi       RWO           Retain          Bound     openrisknet-infra/postgresql-claim                               2h
pv/pv-rabbitmq                                1Gi        RWO           Retain          Bound     openrisknet-infra/rabbitmq-claim                                 2h

NAME                   STATUS    VOLUME          CAPACITY   ACCESSMODES   STORAGECLASS   AGE
pvc/postgresql-claim   Bound     pv-postgresql   50Gi       RWO           standard       2h
pvc/rabbitmq-claim     Bound     pv-rabbitmq     1Gi        RWO           standard       2h
```

> Note: if reusing the postgres PV/PVC you will need to delete the contents of the volume (the 
  `/exports/pv-postgresql` directory) or you may get permissions problems when postgres initialises.

Now we are ready to start deploying the infrastructure.

### Deploy PostgreSQL, RabbitMQ and SSO

Deploy PostgreSQL, RabbitMQ and Keycloak to the infrastructure project:

```
./sso-env-deploy.sh
./sso-deploy.sh
./rabbitmq-deploy.sh
```

To get postgres running in some environments you might need to
set permissions on the PV that is used. e.g.

```
minishift ssh -- sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/pv0091
```
(lookup the appropriate PV to fix)

You may need to run this to fix a bug that prevents Keycloak from starting:
```
oc volume dc/sso --add \
    --mount-path /opt/eap/standalone/configuration/standalone_xml_history \
    --name standalone-xml-history
```

Check that the insrastructure components are all running (e.g. use the web console).
It may take several minutes for everything to start.

Now we can deploy Squonk.

## Squonk Application

Move into the `squonk-app` directory to deploy Squonk to the `squonk` project.

### Squonk Application PVs and PVCs

First specify the how the persistent volumes are to be provided.

#### If using Minshift:
```
oc process -f squonk-pv-minishift.yaml | oc create -f -
```

#### If using NFS with OpenShift

First create NFS exports on the node that is acting as the NFS server (probably the infrastructure node)
for `/exports/squonk-work-dir` and `/exports/squonk-service-descriptors` and then define the PVs and PVCs:

```
oc process -p APP_NAMESPACE=$OC_PROJECT -p NFS_SERVER=$OC_NFS_SERVER -f squonk-pv-nfs.yaml | oc create -f -

```

### Configure Infrastructure

Next configure Squonk to use the infrastructure components. 
This process runs in both the `infrastructure` and `squonk` projects.

```
./squonk-infra-deploy.sh
```

This configures the Squonk application in the `squonk` project to use 
PostgreSQL, RabbitMQ and Keycloak from the `infrastructure` project.

For PostgreSQL it creates the `squonk` database and user and sets permissions.
A secret named `squonk-database-credentials` is created in the `squonk` project.

For RabbitMQ it creates the necessary exchange and users.
A secret named `squonk-rabbitmq-credentials` is created in the `squonk` project.

For Keycloak it creates the client application in the realm as the
`$OC_ADMIN_USER` user using the sso secret credentials for connecting
to Keycloak. A ConfigMap named `squonk-sso-config` is created in the `squonk`
project containing the `keycloak.json` and `context.xml` files that will be
needed to connect the Squonk notebook (portal application) to Keycloak for SSO.

Once running you will need to add roles and user to the Keycloak realm to allow you to test the Squonk notebook
application.

For instance:

-   Create the `standard-user` role
-   Add `standard-user`to the default roles
-   Create sample users e.g. `user1` and assign passwords.

### Squonk Application

Now deploy the Squonk application:

```
./squonk-app-keycloak-deploy.sh
```

This deploys the Squonk application components (cellexecutor, coreservices, chemservices-basic,
portal and related images).

Following this the Computational Notebook should be running.

