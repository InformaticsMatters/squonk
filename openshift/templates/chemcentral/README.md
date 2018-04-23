# Deploying Central

This is work in progress.

## Setup

Login as a standard user e.g. `developer`

Create the project.

```
oc new-project chemcentral
```

## Persistent volumes

### If using Minishift

```
oc process -f chemcentral-pvc-minishift.yaml | oc create -f -
```

