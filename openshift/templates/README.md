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
and the `squonk` (`$OC_USER`) accounts and grant the `admin` user `cluster-admin` role.
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

>   If you're running in minishift and starting from a clean system there's a
    script to simplify the full deployment process. With a suitable minishift
    running and a set of environment variables set you can run
    `./minishift-deploy.sh` to deploy the entire application. Once done
    you can then login to the SSO instance and create your Squonk users
    and then use the Portal.

### Test logins

Ensure that you have `$OC_ADMIN` and `$OC_USER` by testing a login of each.

>   This forces entering the password, which won't be required again in your
    session therefore avoiding the need for oc passwords later in the process.

```
oc login $OC_MASTER_URL -u $OC_USER
oc login $OC_MASTER_URL -u $OC_ADMIN
```

### Create Keycloak image streams

The keycloak deployment is based on that found in the 
[jboss-openshift](https://github.com/jboss-openshift/application-templates/tree/master/sso)
application templates.

As that `admin` user you must deploy the xpaas image streams to your OpenShift environment:

```
oc create -f https://raw.githubusercontent.com/jboss-container-images/redhat-sso-7-openshift-image/sso72-dev/templates/sso72-image-stream.json -n openshift
             
```

This only needs to be done once.

### Create Projects

Create projects as the `$OC_ADMIN_USER` user:
```
oc new-project $OC_PROJECT --display-name='Squonk Applications'
oc new-project $OC_INFRA_PROJECT --display-name='Squonk Infrastructure'
```

>   If you delete the projects you may also need to manually delete the PVs that 
    are created in the next step.


## Create Infrastructure

Move into the `squonk-infra` directory.

### Infrastructure PVs and PVCs

Create the PVs required by Postgres and RabbitMQ.

#### If using Minishift:

Minishift comes with 100 PVs ready to use so you only need to create the PVCs:

```
oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT -f infra-pvc-minishift.yaml | oc create -f -
```

After completing you should see something like this:

```
$ oc get pvc
NAME               STATUS    VOLUME    CAPACITY   ACCESSMODES   STORAGECLASS   AGE
postgresql-claim   Bound     pv0015    100Gi      RWO,ROX,RWX                  11s
rabbitmq-claim     Bound     pv0002    100Gi      RWO,ROX,RWX                  11s
```

To get postgres running in Minishift you may need to
set permissions on the PV that is used. e.g.

```
PG_PVC=$(oc get pvc/postgresql-claim --no-headers | tr -s ' ' | cut -f 3 -d ' ')
minishift ssh -- sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/${PG_PVC}
```

#### If using NFS with OpenShift: 

First create NFS exports on the node that is acting as the NFS server (probably the infrastructure node) 
for `/exports/pv-postgresql` and `/exports/pv-rabbitmq` and then define the PVs and PVCs:

```
oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT -p NFS_SERVER=$OC_NFS_SERVER -p NFS_PATH=$OC_NFS_PATH -f infra-pv-nfs.yaml | oc create -f -
oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT -f infra-pvc-nfs.yaml | oc create -f -
```

This creates PVs for the NFS mounts and binds the PVCs that RabbitMQ and PostgreSQL need. This is 'permanent' coupling
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

>   Note: if re-using these PVs/PVCs you will need to delete the contents of the volume (the
    `/exports/pv-postgresql` and `/exports/pv-rabbitmq` directories) or you may get permissions
    problems when postgres and rabbitmq initialise.

Now we are ready to start deploying the infrastructure.

#### If using dynamic provisioning with OpenShift:

Dymanic provisioning allows to only specify the PVS and OpensShift will satisfy the request dynamically
using whatever dynamic provision is configured. You can use the StorageClass property to define
what type of storage you need.

This is tested with Cinder volumes on OpenStack but other mechanisms should also work.
Dynamic provisioning must be set up on OpenShift before you start.

From the infra project create the PVCs (with OpenShift creating the PVs for you) using:

```
oc process -p STORAGE_CLASS=standard -p POSTGRESQL_VOLUME_SIZE=125Gi -f infra-pvc-dynamic.yaml | oc create -f -
```

>   Note: use whatever value you need for the STORAGE_CLASS and POSTGRESQL_VOLUME_SIZE properties.

>   Note: if re-using the postgres PV/PVC you will need to delete the contents of the volume (the
    `/exports/pv-postgresql` directory) or you may get permissions problems when postgres initialises.


### Deploy PostgreSQL, RabbitMQ and SSO

Deploy PostgreSQL, RabbitMQ and Keycloak to the infrastructure project:

```
./sso-postgres-deploy.sh
./rabbitmq-deploy.sh
```

>   NOTE: With Minishift you may stumble on the defect
    `redhat-sso-7/sso70-openshift image fails to start`
    (https://bugzilla.redhat.com/show_bug.cgi?id=1408453) which manifests
    itself with a _Could not rename /opt/eap/standalone/configuration/standalone_xml_history/current_
    exception and the Pod failing to start. As the `admin` user in the
    `openrisknet-infra` project you should be able to work-aropund the problem
    with the following command:
     
     oc volume dc/sso --add --claim-size 512M --mount-path /opt/eap/standalone/configuration/standalone_xml_history --name standalone-xml-history 

Check that the infrastructure components are all running (e.g. use the web console).
It may take several minutes for everything to start.

There are 3 Pods that we need to wait for. If it's of any use the following
will block until the required pod count is 3:  

```
PODS=$(oc get po --no-headers | grep -v "deploy" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
until [ $PODS -eq 3 ]
do
    PODS=$(oc get po --no-headers | grep -v "deploy" | grep "1/1" | wc -l | tr -s ' ' | cut -f 2 -d ' ')
    sleep 2
done
```

Now we can deploy Squonk.

### Undeploy

Run the `sso-postgres-undeploy.sh` and `rabbitmq-undeploy.sh` scripts to undeploy these applications.
Note that the PVCs are NOT deleted by these scripts to avoid accidental loss of data.
Delete thesee manually if needed.


### Post Install Operations

#### Keycloak Users

Once running you will need to add roles and user to the Keycloak realm
to allow you to test the Squonk notebook application.

>   The keycloak admin login credentials are stored as
    `keycloak-secrets` secrets in the Squonk Infrastructure project.

For instance:

-   In `Configure -> Roles` of the `Squonk` _realm_, create a `standard-user`
    role and then add it to the list of _Default Roles_
-   In `Manage -> Users` create sample users e.g. `user1` and assign a
    password in the `Credentials` tab and make sure the password is _not_
    `Temporary`.
    
### Identity providers

To allow end users to login using LinkedIn as an identity provider add LinkedIn as an identity provider for the realm.
You need to create a LinkedIn OAuth app from here: https://www.linkedin.com/developer/apps

To allow end users to login using GitHub as an identity provider add GitHub as an identity provider for the realm.
You need to create a GitHub OAuth app from here: https://github.com/settings/developers

### Email settings

Go to the email tab of the Keycloak realm and enter the details of your SMTP server.

#### Keycloak TLS certificate

Once you are happy with the deployment switch it over to use trusted TLS certificates provided through Let's Encrypt.
Edit the YAML definition for the SSO Route and change the annotation that activate the ACME controller by changing the 
`kubernetes.io/tls-acme` annotation to true like this:

```
metadata:
  annotations:
    kubernetes.io/tls-acme: 'true'
```


## Squonk Application

Move into the `squonk-app` directory to deploy Squonk to the `squonk` project.

### Squonk Application PVs and PVCs

First specify the how the persistent volumes are to be provided.

#### If using Minshift:
```
oc process -p APP_NAMESPACE=$OC_PROJECT -f squonk-pvc-minishift.yaml | oc create -f -
```

#### If using NFS with OpenShift

First create NFS export on the node that is acting as the NFS server (probably the infrastructure node)
for `/exports/squonk-work-dir` and then define the PVs and PVCs:

```
oc process -p APP_NAMESPACE=$OC_PROJECT -p NFS_SERVER=$OC_NFS_SERVER -p NFS_PATH=$OC_NFS_PATH -f squonk-pv-nfs.yaml | oc create -f -
oc process -p APP_NAMESPACE=$OC_PROJECT -f squonk-pvc-nfs.yaml | oc create -f -
```

#### If using dynamic provisioning with OpenShift:

This requires a dynamic provisioner that support ReadWriteMany storage type e.g. GlusterFS. 
The STORAGE_CLASS parameter let's you specify a storage class if `glusterfs-storage` is not suitable.
If a suitable storage class is not avaiable then use NFS.

```
oc process -f squonk-pvc-dynamic.yaml | oc create -f -

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

To confirm that the keycloak initialisation has completed run this:
```
oc logs job/squonk-client-creator -n $OC_INFRA_PROJECT
```
The output should end with `Registered client squonk-notebook in realm [...]`


Make sure you have created the `standard-user` role and the required users in Keycloak (see above).

### Squonk Application

Now deploy the Squonk application:

```
./squonk-app-keycloak-deploy.sh
```

This deploys the Squonk application components (cellexecutor, coreservices, chemservices-basic,
portal and related images).

Following this the Computational Notebook should be running.

### Chemcentral

Once Squonk is deployed you can deploy the chemcentral chemical search application.
This is described [here](chemcentral/README.md).

## Problems

Since this was written problems deploying relating to permssions on the PVs have been encountered. Neither the postgresql nor the rabbitmq containers are able to start as the permissions on the file system are wrong. It is not currently clear what has changed since these instructions were written, and this needs to be investigated. Workarounds are described here.

To fix the problem with rabbitmq find the rabbitmq pod which is failing to deploy and do a `oc debug <pod-name>`. You will need to scale the failed pod to zero or cancel the deployment so that the volume can be mounted into the debug pod. The pod runs as root so permissions can be changed using `chmod rabbitmq.rabbitmq /volume/mnesia`. Then exit the debug pod and redeploy.

Fixing postgresql is slightly more complex as the container runs as the postgres user which does not have permissions to change the ownership of the required directory as it is owned by root. Create a debug pod for postgresq as for rabbitmq. Identify the node the pod is running on and SSH to it. Then do a docker exec into the container usiing `sudo docker exec -it -u 0:0 <container-name> bash` and then execute `chown postgres.root /var/lib/pgsql/data`. Exit the container and the debug pod and redeploy.

