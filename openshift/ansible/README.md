# Squonk Ansible OpenShift Deployment
You can run the playbook from this directory with the command: -

    ansible-playbook site.yaml

## Prerequisites
Before running the playbook: -

1.  You're on the bastion node
1.  You have installed Ansible (any version from 2.5)
1.  The `oc` command-set is available to you as a user
1.  An OpenShift cluster has been installed
1.  There is an `admin` user known to the cluster
1.  There is a `developer` user known to the cluster
1.  You have setup your own `setenv.sh` and run `source setenv.sh`

If using NFS the following NFS volumes are required for a _full_ installation
on the bastion `/data` directory: -

*   pv-rabbitmq
*   pv-postgresql
*   pv-chemcentral-loader
*   pv-chemcentraldb
*   squonk-work-dir

## Creating encrypted secrets
You can safely encrytpt varibale value using `ansible-vault`. There
are a number of sensitive values already encrypted
(see `squonk/defaults/main.yaml`).
 
We typically use a separate encryption password for every playbook. the ansible vault password you can encrypt strings
for the `defaults/main.yaml` file by running something like this: -

    ansible-vault encrypt_string "<string>" \
        --name <string name> --ask-vault-pass
