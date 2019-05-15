# Instructions for Squonk install using Keycloak.

By convention we refer to the project containing the infrastructure components
(PostgreSQL, RabbitMQ, Keycloak) as the `squonk-infra` project but this can
be set using the `OC_INFRA_PROJECT` environment variable.

By convention we refer to the project containing the application components
(portal, cellexectuor, coreservices, chemservices-basic ...) as the `squonk`
project but this can be set using the `OC_PROJECT` environment variable.

Deployment is automated through the use of a number of Ansible playbooks
and roles, the details of which 
 
The process is broken into a number of key parts:

1.  Deploy the **infrastructure** needed by Squonk
2.  Deploy **Squonk** application components.
3.  Deploy Squonk's **ChemCentral** Database
4.  Deploy Squonk **pipelines**

Before doing this you need to do some basic setup, so let's get started.

## Setup

### Configure the Installation
You will need to provide two files: -

-   A `setenv.sh` file to define a small number of sensitive variables
-   A _params_ file to define non-sensitive deployment variables

Before you start you must have or create the OpenShift `admin` account
(`OC_ADMIN_USER`) and its password (`OC_ADMIN_PASSWORD`). If you're using
the [OKD Orchestrator] to create your cluster this will have been done for you.

Now back on the node where the installation will be deployed from
(the Ansible _control_ node) where you have this repository cloned, i.e.
the bastion node or your workstation where you have Ansible and the `oc`
command-set: -

Create/edit `setenv.sh` from an existing file and prepare a set of Ansible
parameters in a `params-???.yaml` file and populate suitable variable
values. Once done, _source_ the setenv file...

    $ source setenv.sh

>   If you're using minishift you can use the pre-prepared
    environment file `setenv-minishift.sh`, which is ready to run and does not
    need any initial users.
    
The installation is handled by a number of ansible playbooks where its
[README](../ansible/README.md) explains the installation steps for
the infrastructure, squonk and its pipelines. To continue with
deployment follow its instructions.

>   If you're not using dynamic/gluster volumes you will need to provision
    NFS volumes, as the playbooks do not do this. Refer to the
    **NFS Considerations** section later in this README.

>   You can start with the environment file that's ready for minishift
    or one of the encrypted productions deployments (`setenv-orn-dev.sh.vault`
    and `setenv-orn-prod.sh.vault`), but you'll need to vault password to
    decrypt these production files.

### Post Install Operations (infrastructure)

### Keycloak Users

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

To allow end users to login using LinkedIn as an identity provider add
LinkedIn as an identity provider for the realm. You need to create a LinkedIn
OAuth app from here: https://www.linkedin.com/developer/apps

To allow end users to login using GitHub as an identity provider add GitHub
as an identity provider for the realm. You need to create a GitHub OAuth app
from here: https://github.com/settings/developers

### Email settings

Go to the email tab of the Keycloak realm and enter the details
of your SMTP server.

### Keycloak TLS certificate

Once you are happy with the deployment switch it over to use trusted TLS
certificates provided through Let's Encrypt. Edit the YAML definition for
the SSO Route and change the annotation that activate the ACME controller by
changing the  `kubernetes.io/tls-acme` annotation to true like this:

    metadata:
      annotations:
        kubernetes.io/tls-acme: 'true'

## NFS Considerations

### Infrastructure
First create NFS exports on the node that is acting as the NFS server
(probably the infrastructure node)  for `/exports/pv-postgresql` and
`/exports/pv-rabbitmq` and then define the PVs and PVCs:

    oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT \
        -p NFS_SERVER=$OC_NFS_SERVER -p NFS_PATH=$OC_NFS_PATH \
        -f infra-pv-nfs.yaml | oc create -f -
    oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT -f infra-pvc-nfs.yaml | oc create -f -

This creates PVs for the NFS mounts and binds the PVCs that RabbitMQ and
PostgreSQL need. This is 'permanent' coupling of the PVC to the PV so that
this (and any data in the NFS mounts) can be retained between deployments.

Following this you should see something like this (irrelevant entries are excluded):

    $ oc get pv,pvc
    NAME                                          CAPACITY   ACCESSMODES   RECLAIMPOLICY   STATUS    CLAIM                                   STORAGECLASS   REASON    AGE
    pv/pv-postgresql                              50Gi       RWO           Retain          Bound     openrisknet-infra/postgresql-claim                               2h
    pv/pv-rabbitmq                                1Gi        RWO           Retain          Bound     openrisknet-infra/rabbitmq-claim                                 2h
    
    NAME                   STATUS    VOLUME          CAPACITY   ACCESSMODES   STORAGECLASS   AGE
    pvc/postgresql-claim   Bound     pv-postgresql   50Gi       RWO           standard       2h
    pvc/rabbitmq-claim     Bound     pv-rabbitmq     1Gi        RWO           standard       2h

>   Note: if re-using these PVs/PVCs you will need to delete the contents of
    the volume (the `/exports/pv-postgresql` and `/exports/pv-rabbitmq`
    directories) or you may get permissions problems when postgres and
    rabbitmq initialise.

### Squonk
First create NFS export on the node that is acting as the NFS server
(probably the infrastructure node) for `/exports/squonk-work-dir` and
then define the PVs and PVCs:

    oc process -p APP_NAMESPACE=$OC_PROJECT -p NFS_SERVER=$OC_NFS_SERVER \
        -p NFS_PATH=$OC_NFS_PATH -f squonk-pv-nfs.yaml | oc create -f -
    oc process -p APP_NAMESPACE=$OC_PROJECT -f squonk-pvc-nfs.yaml | oc create -f -

## Problems

1.  Since this was written problems deploying relating to permissions on the PVs
    have been encountered. Neither the postgresql nor the rabbitmq containers
    are able to start as the permissions on the file system are wrong.
    It is not currently clear what has changed since these instructions were
    written, and this needs to be investigated. Workarounds are described here.

    To fix the problem with rabbitmq find the rabbitmq pod which is failing to
    deploy and do a `oc debug <pod-name>`. You will need to scale the failed
    pod to zero or cancel the deployment so that the volume can be mounted
    into the debug pod. The pod runs as root so permissions can be changed
    using `chmod rabbitmq.rabbitmq /volume/mnesia`. Then exit the debug pod
    and redeploy.

    Fixing postgresql is slightly more complex as the container runs as the
    postgres user which does not have permissions to change the ownership
    of the required directory as it is owned by root. Create a debug pod
    for postgresq as for rabbitmq. Identify the node the pod is running on
    and SSH to it. Then do a docker exec into the container using
    `sudo docker exec -it -u 0:0 <container-name> bash` and then execute
    `chown postgres.root /var/lib/pgsql/data`. Exit the container and the
    debug pod and redeploy.

---

[okd orchestrator]: https://github.com/InformaticsMatters/okd-orchestrator.
