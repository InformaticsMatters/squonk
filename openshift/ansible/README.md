# Squonk Ansible OpenShift Deployment
You can run the infrastructure and squonk playbook from this
directory with the commands: -

    ansible-playbook playbooks/squonk-infra/deploy.yaml
    ansible-playbook playbooks/squonk/deploy.yaml
    ansible-playbook playbooks/squonk-chemcentral/deploy.yaml

>   Remember to first run the appropriate `setenv.sh` script.

And you can provide users 'after-the-fact' with: -

    ansible-playbook -e "users_file=users.txt" -t keycloak-users \
        playbooks/squonk/deploy.yaml
        
You can delete the ChemCentral loader and re-run it with: -

    ansible-playbook playbooks/squonk-chemcentral/delete-loader.yaml
    ansible-playbook -t chemcentral-loader playbooks/squonk-chemcentral/deploy.yaml
        
There is an `undeploy` playbook that can be run for squonk: -

    ansible-playbook playbooks/squonk/undeploy.yaml

## Prerequisites
Before running the playbook: -

1.  You're on the bastion node
1.  You have installed Ansible (any version from 2.5)
1.  The `oc` command-set is available to you as a user
1.  An OpenShift cluster has been installed
1.  There is an `admin` user known to the cluster
1.  There is a `developer` user known to the cluster
1.  You have setup your own `setenv.sh` (typically in `openshift/templates`)
    and you have run `source setenv.sh` using it.

If using NFS, it is correctly configured with appropriate
disk provisioning ready for each PV that expects a volume.
