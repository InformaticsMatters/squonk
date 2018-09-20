# Squonk Ansible OpenShift Deployment
You can run the infrastructure and squonk playbook from this
directory with the commands: -

    ansible-playbook playbooks/squonk-infra/deploy.yaml
    
    ansible-playbook playbooks/squonk/deploy.yaml

There are `undeploy` playbooks that can be run for both the
infrastructure and squonk: -

    ansible-playbook playbooks/squonk/undeploy.yaml

## Prerequisites
Before running the playbook: -

1.  You're on the bastion node
1.  You have installed Ansible (any version from 2.5)
1.  The `oc` command-set is available to you as a user
1.  An OpenShift cluster has been installed
1.  There is an `admin` user known to the cluster
1.  There is a `developer` user known to the cluster
1.  You have setup your own `setenv.sh` and run `source setenv.sh`

If using NFS, it is correctly configured with appropriate
provisioning for each PV that expects a volume.

## Creating encrypted secrets
You can safely encrytpt varibale value using `ansible-vault`. There
are a number of sensitive values already encrypted
(see `squonk/defaults/main.yaml`).
 
We typically use a separate encryption password for every playbook. the ansible vault password you can encrypt strings
for the `defaults/main.yaml` file by running something like this: -

    ansible-vault encrypt_string "<string>" \
        --name <string name> --ask-vault-pass
