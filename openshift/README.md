# Squonk to Minishift

## Deployment

### Chemservices

Template in openshift/templates/chemservices.yaml

Create user and project.

As system user run this:

```
oc adm policy add-scc-to-group anyuid system:authenticated
```

Deploy using:

```
oc process -f openshift/templates/chemservices.yaml -o yaml | oc create -f -
```


## TODOs

Avoid running chemservices image as root user. Modify Dockerfile to run as tomcat user (see example-servlet for how to do this). Ideally allow to run as openshift gernerated user id.


