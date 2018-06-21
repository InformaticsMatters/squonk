# Deploying ChemCentral

This is work in progress.

## Setup

We assume the following setup:

1. An infrastructure project (e.g. `squonk-infra`) where the database will be deployed
1. An application project (`squonk`) where the search service will be deployed
1. These have already been created by deploying the main Squonk application


## Service account

In the `squonk-infra` project as an admin user e.g. `admin`:

Create the `chemcentral-postgres` service account:
```
oc create sa chemcentral-postgres
```

Adding anyuid role to that service account:
```
oc adm policy add-scc-to-user anyuid -z chemcentral-postgres
```

## Persistent volumes

### If using Minishift

```
oc process -f chemcentral-pvc-minishift.yaml | oc create -n $OC_INFRA_PROJECT -f -
```

You now need to fix the permissions on the PV directory like this:
```
minishift ssh -- sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/pv0091
```
Adjust the directory name according to what the PVC has grabbed.

### If using NFS with OpenShift

First create NFS export on the node that is acting as the NFS server (probably the infrastructure node)
for `/exports/pv-chemcentraldb`. You need to specify NFS export options as `*(rw,sync,no_subtree_check,no_root_squash)`
due to strange permissions problems relating to NFS (TODO - resolve this).

Then define the PVs and PVCs:

```
oc process -p NFS_SERVER=$OC_NFS_SERVER -p NFS_PATH=/data -f chemcentral-pvc-nfs.yaml | oc create -n $OC_INFRA_PROJECT -f -

```



### If using dynamic provisioning

```
oc process -p INFRA_NAMESPACE=$OC_INFRA_PROJECT -p NFS_SERVER=$OC_NFS_SERVER -p PVC_SIZE=100Gi -f chemcentral-pvc-nfs.yaml | oc create -n $OC_INFRA_PROJECT -f -
```

Adjust the parameters as needed and look in the `chemcentral-pvc-dynamic.yaml` for additional parameters that
can be specified.

## Deploy

Deploy the chemcentral PostgreSQL database with the RDKit cartridge and the chemcentral search deployments.

```
./deploy.sh
```

## Loading data

General information about the Dockerised process for loading data into the ChemCentral database is [here](../../../StructureDatabases.md).

To run this in an openshift environment you must do this:

Switch to the `squonk` project:
```
oc project $OC_PROJECT
```

### Create a PVC for the files to load.

#### If using Minishift

```
oc process -f chemcentral-data-loader-pvc-minishift.yaml | oc create -f -
```


#### If using NFS with OpenShift

Create an NFS export named `pv-chemcentral-loader`. Then:

```
oc process -p NFS_SERVER=$OC_NFS_SERVER -f chemcentral-data-loader-pvc-nfs.yaml | oc create -f -
```

### Provide the datafile

Copy the datafile to be loaded to the PV that is used by that PVC.

On Minishift do something like this:

```
$ minishift ssh
# sudo -i
# cd /mnt/sda1/var/lib/minishift/openshift.local.pv/pv0005
# wget http://downloads.emolecules.com/orderbb/2018-06-01/version.smi.gz
```

Make sure the file is world readable.

### Run the loader

```
oc process -f loader.yaml -p LOADER_CLASS=org.squonk.rdkit.db.loaders.EMoleculesBBSmilesLoader -p LOADER_FILE=version.smi.gz -p LIMIT=20000 | oc create -f -
```

## Test it works

To test substructure search from the database:

```
$ oc rsh db-chemcentral-1-kftmw
# psql -U chemcentral -d chemcentral
psql (9.5.10)
Type "help" for help.

chemcentral=> select count(*) from vendordbs.emolecules_order_bb_molfps WHERE m@>'c1cccc2c1CNCCN2';
 count 
-------
    10
(1 row)

chemcentral=> \q
# exit
$
```

### Clean up

To remove the job to allow for another run:

```
oc delete job/chemcentral-loader
```