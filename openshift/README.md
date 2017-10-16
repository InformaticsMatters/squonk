# Squonk to Minishift

## Deployment
The deployment consists of a number of templates and stages:

-   secrets
-   squonk-infra
-   squonk-app
-   portal

### Secrets
Secrets need to be deployed before any other object.

    $ oc process -f secrets.yaml | oc create -f -
    
### Infrastructure
This is responsible for the operating infrastructure, which
consists of `postgres` and `rabbitmq`. Before you start, as a system user
run the following command, to allow container to specify to run as tomcat user:
    
    $ oc adm policy add-scc-to-group anyuid system:authenticated

Then, deploy using:

    $ oc process -f squonk-infra.yaml | oc create -f -

### Application
>   The Application should be dependent on the infrastructure so, in theory,
    there's no need to wait for Infrastructure before you deploy these
    services.

Deploy using:

    $ oc process -f squonk-app.yaml | oc create -f -

### Portal
>   The Portal should be dependent on core services so, in theory,
    there's no need to wait for the Application before you deploy these
    services.

Deploy using:

    $ oc process -f portal.yaml | oc create -f -

## TODOs

-   Avoid running chemservices image as Openshift assigned user. (priority=low)
-   Split Chemservices up into its individual services and deploy independently. 
    Only rdkit-search will require access to Postgres and RabbitMQ. (priority=medium)
