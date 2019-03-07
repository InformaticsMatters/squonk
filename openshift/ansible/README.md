# Squonk Ansible OpenShift Deployment

If using MiniShift see the instructions at the bottom of this page that must be
executed first to set up the MiniShift environment. After that the deployment is
the same as for OpenShift.

You can run the infrastructure and squonk playbook from this
directory with the commands: -

    ansible-playbook playbooks/infra/deploy.yaml
    ansible-playbook playbooks/squonk/deploy.yaml
    ansible-playbook playbooks/squonk-chemcentral/deploy.yaml

>   Remember to first `source` an appropriately crafted
    `../env/setenv.sh` script first!

You can add users from a text file (that contains one user and space-separated
password per line) 'after-the-fact' by defining the `user_file` playbook
variable and then limiting the deployment to just the tasks relating to
_keycloak users_ with the following: -

    ansible-playbook -e "users_file=example-users.txt" -t keycloak-users \
        playbooks/squonk/deploy.yaml

>   `example-users.txt` is a demo file. You can use the file `users.txt`
    to safely add your own users. It is prevented form being committed to
    Git as it's listed in the project's `.gitignore` file.

In order to load data into the ChemCentral database you will need to prepare
the loader data volume with suitable source data (running a relevant
**prep-loader** playbook) before running a **loader**.

Loading data requires: -

1.  Creating a volume to store the source data
    (using the `create-loader-volume` playbook)
1.  Loading data using a suitable `prep-loader` playbook. This creates the
    loader volume (just in case you forgot) and then runs task that runs
    an OpenShift `Job` template which does the preparation (downloading).
    Inspect the exiting `prep-loader` playbooks,
    their matching role tasks (typically called
    `roles/squonk-chemcentral/tasks/prep-loader-<something>`)
    and the matching OpenShift templates
1.  Running a loader playbook (like `run-loader`)

As an example, you can prepare and load the example/free eMolecules
data set with the following...

    ansible-playbook playbooks/squonk-chemcentral/prep-loader-emolecules.yaml
    ansible-playbook playbooks/squonk-chemcentral/run-loader.yaml \
        -e loader_class=EMoleculesBBSmilesLoader \
        -e loader_file=version.smi.gz

>   Variables that control the `run-loader` playbook are defined in
    `roles/squonk-chemcentral/defaults/main.yaml`
    
When loading is complete you can remove the loader volume created and used by
the preparation task: -

    ansible-playbook playbooks/squonk-chemcentral/delete-loader-volume.yaml

You can delete the ChemCentral loader and re-run it with: -

    ansible-playbook playbooks/squonk-chemcentral/delete-loader.yaml
    ansible-playbook playbooks/squonk-chemcentral/run-loader.yaml
        
There is an `undeploy` playbook that can be run for squonk: -

    ansible-playbook playbooks/squonk/undeploy.yaml

and for ChemCentral: -

    ansible-playbook playbooks/squonk-chemcentral/undeploy.yaml

and for the infrastructure: -

    ansible-playbook playbooks/infra/undeploy.yaml

## Playbooks for the infrastructure database
Other applications might want to use the PostgreSQL database that is deployed
in the Infrastructure project. Typically you might want to create a new
database and create a user for that database. You can do this using two
convenient playbooks (refer to them for documentation): -

    ansible-playbook playbooks/infra/create-user-db.yaml \
        -e db=mydb \
        -e db_user=me \
        -e db_namespace=myproject
        
    ansible-playbook playbooks/infra/delete-user-db.yaml \
        -e db=mydb \
        -e db_user=me

The result will be secrets created in your project
(`myproject` in the above example) containing  the database credentials that
your application can use. This process is used during the Squonk 
deployment to create the database that it uses.

## Prerequisites
Before running the playbooks: -

1.  You're on the bastion node, or you are on a node with...
    1.  Ansible installed (any version from 2.5)
    1.  The `oc` command-set is available to you as a user
1.  An OpenShift cluster has been installed
1.  There is an `admin` user known to the cluster
1.  There is a `developer` user known to the cluster
1.  You have setup your own `setenv.sh` (typically in `openshift/env`)
    and you have run `source setenv.sh` using it.

If using NFS, it is correctly configured with appropriate
disk provisioning ready for each PV that expects a volume -
the Ansible playbooks do not setup NFS.

## MiniShift considerations
While it's a work-in-progress, support for some versions of MiniShift is
available. We've tested with: -

-   OpenShift 3.9.0 (MiniShift 1.25.0)
-   OpenShift 3.11.0 (MiniShift 1.27.0, 1.32.0)
-   VirtualBox 5.2.20 (OSX)
-   MiniShift 1.25.0, 1.26.1, 1.27.0, 1.31.0 and 1.32.0 

Start MiniShift (pre-1.26) with something like: -

    minishift start --cpus 4 --memory 8GB --disk-size 40GB \
        --openshift-version 3.9.0 --vm-driver virtualbox

>   If you're using MiniShift v1.26 or later you cannot use the OpenShift
    v3.9.0 image, you must move to OpenShift v3.10.0 or later.
 
You need to setup a suitable `setenv.sh` (and source it).
`openshift/env/setenv-minishift.sh` should work in most cases.
Then run the `minishift` playbook to prepare the cluster **before** running
the above Squonk plays. From this directory, run: -

    $ source ../env/setenv-minishift.sh
    $ ansible-playbook playbooks/minishift/prepare.yaml

>   The MiniShift installation does not use trusted certificates so
    you need to instruct your browser to ignore the security concerns
    that this causes.
