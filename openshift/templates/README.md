# Basic instructions for Squonk install using Keycloak.

This is work in progress and needs to be further streamlined.
The idea is to allow the name of the *-infra and the squonk projects to be changed
(and for them to be the same). The configuration is controlled by the contents of
the `setenv.sh` file.

By convention we refer to the project containing the infrastructure components (PostgreSQL, RabbitMQ, Keycloak) as the 
`squonk-infra` project but this can be set using the $OC_INFRA_PROJECT environment variable.
By convention we refer to the project containing the application components
(portal, cellexectuor, coreservices, chemseervices-basic ...) as the `squonk` project
but this can be set using the $OC_PROJECT environment variable.
 
The process is broken into two key parts:

1.  Deploy the infrastructure needed by Squonk. This includes PostgreSQL,
    Keycloak and RabbitMQ. This might already be deployed,
    for instance, if running in an OpenRiskNet environment.
    Files for doing this are in the squonk-infra directory.
2.  Deploy the Squonk application components.
    Files for doing this are in the squonk-app directory.

Before you run this you must create admin (`$OC_ADMIN_USER`)
and squonk (`$OC_USER`) accounts and give the admin user cluster-admin role
(as the system:admin user):
```
oc adm policy add-cluster-role-to-user cluster-admin admin
```

As `system:admin` you must also deploy the xpaas image streams to your
OpenShift environment:
```
oc create -f https://raw.githubusercontent.com/openshift/openshift-ansible/master/roles/openshift_examples/files/examples/v3.6/xpaas-streams/jboss-image-streams.json -n openshift
```

Now we are ready to start deploying.

Create/edit `setenv.sh` from the supplied template. At the very least you
need to define `OC_MASTER_HOSTNAME`, which in minishift is likely to be
something like `192.168.99.100`. Once done, _source_ the file...

```
source setenv.sh
```

## Squonk Infrastructure

Move into the `squonk-infra` directory.

Create the certificates used by Keycloak.
The certs and keystores are protected by a single password that
is specified as the `$OC_CERTS_PASSWORD` variable.

```
./certs-create.sh
```

Create projects as the `$OC_ADMIN` user:
```
oc new-project $OC_INFRA_PROJECT
oc new-project $OC_PROJECT
```

Deploy PostgreSQL, Keycloak and RabbitMQ to the `squonk-infra` project:
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
oc volume dc/sso --add --claim-size 512M \
    --mount-path /opt/eap/standalone/configuration/standalone_xml_history \
    --name standalone-xml-history
```
    
Once running you will need to add roles and user to the Keycloak realm.
For instance:

-   Create the `standard-user` role
-   Add `standard-user`to the default roles
-   Create sample users e.g. `user1` and assign passwords.

## Squonk Application

Move into the `squonk-app` directory to deploy Squonk to the `squonk` project.

### Persistent volumes
First specify the how the persistent volumes are to be provided.

On Minshift:
```
oc process -f squonk-pv-minishift.yaml | oc create -f -
```

On OpenShift using NFS first create NFS mounts named `squonk-work-dir`
and `core-service-descriptors` and then define the PVs and PVCs:
```
oc process -f squonk-pv-nfs.yaml | oc create -f -
```

### Infrastructure
Next configure the infrastructure. This process runs in both the infrastructure
and Squonk projects.

```
./squonk-infra-deploy.sh
```
This configures PostgreSQL, RabbitMQ and Keycloak for the Squonk application.

For PostgreSQL it creates the `squonk` database and user and sets permissions.
A secret named `squonk-database-credentials` is created in the `squonk` project.

For RabbitMQ it creates the necessary exchange and users.
A secret named `squonk-rabbitmq-credentials` is created in the `squonk` project.

For Keycloak it creates the client application in the realm as the
`$OC_ADMIN_USER` user using the sso secret credentials for connecting
to Keycloak. A ConfigMap named `squonk-sso-config` is created in the `squonk`
project containing the `keycloak.json` and `context.xml` files that will be
needed to connect the Squonk notebook (portal application) to Keyclaok for SSO.

### Squonk Application
Then deploy the Squonk application:
```
./squonk-app-keycloak-deploy.sh
```
This deploys the Squonk application components
(cellexecutor, coreservices, chemservices-basic, portal and related images).
Following this the Computational Notebook should be running.
