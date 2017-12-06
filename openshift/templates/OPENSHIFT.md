Basic instructions for Squonk install using Keycloak.
This is work in progress and needs to be further streamlined.
The idea is to allow the name of the *-infra and the squonk projects to be changed (and for them to be the same). 
The configuration is controlled by the contents of
the `setenv.sh` file.
By convention we refer to the project containing the infrastructure components (PostgreSQL, RabbitMQ, Keycloak) as the 
`squonk-infra` project but this can be set using the $OC_INFRA_PROJECT environment variable.
By convention we refer to the project containing the application components (portal, cellexectuor, coreservices, chemseervices-basic ...)
as the `squonk` project but this can be set using the $OC_PROJECT environment variable.
 
The process is broken into two key parts:
1. Deploy the infrastructure needed by Squonk. This includes PostgreSQL, Keycloak and RabbitMQ. This might already be deployed, 
for instance, if runnning in an OpenRiskNet environment. Files for doing this are in the squonk-infra directory.
2. Deploy the Squonk application components. Files for doing this are in the squonk-app directory.

Before you run this you must create admin ($OC_ADMIN_USER) and squonk ($OC_USER) accounts
and give the admin user cluster-admin role (as system:admin):
`oc adm policy add-cluster-role-to-user cluster-admin admin`

You must also deploy the xpaas image streams to your OpenShift environment:
`oc create -f https://raw.githubusercontent.com/openshift/openshift-ansible/master/roles/openshift_examples/files/examples/v3.6/xpaas-streams/jboss-image-streams.json -n openshift`

1. Now we are ready to start deploying.
1.1 Create/edit setenv.sh
1.2 `source setenv.sh`

## Squonk Infrastructure

Move into the squonk-infra directory

2. Create the certificates used by Keycloak.
The certs and keystores are protected by a single password that is spacified as the $OC_CERTS_PASSWORD variable.
2.1 `./certs-create.sh`

3. Create projects as the $OC_ADMIN_USER user:
3.1. `oc new-project $OC_INFRA_PROJECT`
3.2. `oc new-project $OC_PROJECT`

2. Deploy PostgreSQL, Keycloak and RabbitMQ to the `squonk-infra` project:
2.1. `./sso-env-deploy.sh`
2.2. `./sso-deploy.sh`
2.3. `./rabbitmq-deploy.sh`

To get postgres running in some environments you might need to set permissions on the PV that is used. e.g.
```
minishift ssh -- sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/pv0091
```
(lookup the appropriate PV to fix)

You may need to run this to fix a bug that prevents Keycloak from starting:
`oc volume dc/sso --add --claim-size 512M \
    --mount-path /opt/eap/standalone/configuration/standalone_xml_history \
    --name standalone-xml-history`
    
Once running you will need to add roles and user to the Keycloak realm.
For instance:
1. Create the `standard-user` role
1. Add `standard-user`to the default roles
1. Create sample users e.g. `user1` and assign passwords.

## Squonk Application

Move into the squonk-app directory

3. Deploy Squonk to the `squonk` project:
3.1. First configure the infrastructure: `./squonk-infra-deploy.sh`
This configures PostgreSQL, RabbitMQ and Keycloak for the Squonk application.
For PostgreSQL it creates the `squonk` database and user and sets permissions. A secret named `squonk-database-credentials` is created in the `squonk` project.
For RabbitMQ it creates the necessary exchange and users. A secret named `squonk-rabbitmqe-credentials` is created in the `squonk` project.
For Keycloak it creates the client application in the realm as the $OC_ADMIN_USER user using the sso secret for credential for connecting to Keycloak.
A ConfigMap named `squonk-sso-config` is created in the `squonk` project containing the keycloak.json and context.xml files that will be needed
to connect the Squonk notebook (portal application) to Keyclaok for SSO.

3.2. Then deploy the Squonk application: `./squonk-app-keycloak-deploy.sh`
This deploys the Squonk application components (cellexecutor, coreservices, chemservices-basic, portal and related images).
Following this the Computational Notebook should be running.


