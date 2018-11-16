# Squonk Ansible OpenShift Deployment
You can run the infrastructure and squonk playbook from this
directory with the commands: -

    ansible-playbook playbooks/squonk-infra/deploy.yaml
    ansible-playbook playbooks/squonk/deploy.yaml
    ansible-playbook playbooks/squonk-chemcentral/deploy.yaml

>   Remember to first `source` an appropriately crafted
    `../templates/setenv.sh` script first!

You can add users from a text file (that contains one user and space-separated
password per line) 'after-the-fact' by defining the `user_file` playbook
variable and then limiting the deployment to just the tasks relating to
_keycloak users_ with the following: -

    ansible-playbook -e "users_file=example-users.txt" -t keycloak-users \
        playbooks/squonk/deploy.yaml

>   `example-users.txt` is a demo file. You can use the file `users.txt`
    to safely add your own users. It is prevented form being committed to
    Git as it's listed in the project's `.gitignore` file.

You can delete the ChemCentral loader and re-run it with: -

    ansible-playbook playbooks/squonk-chemcentral/delete-loader.yaml
    ansible-playbook playbooks/squonk-chemcentral/run-loader.yaml
        
There is an `undeploy` playbook that can be run for squonk: -

    ansible-playbook playbooks/squonk/undeploy.yaml

and for ChemCentral: -

    ansible-playbook playbooks/squonk-chemcentral/undeploy.yaml

and for the infrastructure: -

    ansible-playbook playbooks/squonk-infra/undeploy.yaml

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
disk provisioning ready for each PV that expects a volume -
the Ansible playbooks do not setup NFS.

## MiniShift considerations
While it's a work-in-progress, support for some versions of MiniShift is
available. We've tested with: -

-   OpenShift 3.9.0 (MiniShift 1.25.0)
-   OpenShift 3.11.0 (MiniShift 1.27.0)
-   VirtualBox 5.2.20 (OSX)
-   MiniShift 1.25.0, 1.26.1 and 1.27.0

Start MiniShift (pre-1.26) with something like: -

    minishift start --cpus 4 --memory 8GB --disk-size 40GB \
        --openshift-version 3.9.0 --vm-driver virtualbox

>   If you're using MiniShift v1.26 or later you cannot use the OpenShift
    v3.9.0 image, you must move to OpenShift v3.10.0 or later.
 
You need to setup a suitable `setenv.sh` (and source it) and then run the
`minishift` playbook to prepare the cluster **before** running
the above Squonk plays. From this directory, run: -

    ansible-playbook playbooks/minishift/prepare.yaml
