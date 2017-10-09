# Squonk to Minishift

## Deployment

### Chemservices

Template in openshift/templates/chemservices.yaml

Create user and project.

As system user run this to allow container to specify to run as tomcat user:

```
oc adm policy add-scc-to-group anyuid system:authenticated
```

Deploy using:

```
oc process -f openshift/templates/chemservices.yaml -o yaml | oc create -f -
```


## TODOs

Avoid running chemservices image as Openshift assigned user. (priority=low)

Split Chemservices up into its individual services and deploy independently. 
Only rdkit-search will require access to Postgres and RabbitMQ. (priority=medium)


