# Squonk Ansible OpenShift Deployment

## Prerequisites
Before running the playbooks: -

1.  You're on the bastion node, or you are on a node with...
    1.  Ansible installed (any version from 2.7)
    1.  The `oc` command-set is available to you as a user
1.  An OpenShift cluster has been installed
1.  There is an `admin` user known to the cluster
1.  There is a `developer` user known to the cluster
1.  You have setup your own `setenv.sh` (typically in `openshift/env`),
    created a suitable `params` file and you have run `source setenv.sh`.
1.  You have Python (ideally 3.7) and satisfy the requirements
    with something like `pip install -r requirements.txt` from this directory.

>   If using NFS, it is correctly configured with appropriate
    disk provisioning ready for each PV that expects a volume -
    the Ansible playbooks do not setup NFS. So you will typically need mounts
    and exports at `/nfs-orn-infra/pv-backup`, `/nfs-orn-infra/pv-rabbitmq` and
    `/nfs-orn-infra/pv-postgresql` in order to satisfy the infrastructure
    deployment. 

## Deploying the application's infrastructure components
The infrastructure contains important components like RabbitMQ, PostgreSQl
and Keycloak. You can run the infrastructure playbook from this
directory with the command: -

>   Remember to first `source` an appropriately crafted
    `../env/setenv.sh` script first!

>   If using Minishift then please refer to the instructions at the bottom
    of this page, these must be executed first so that the Minishift
    environment is configured properly. Once done the deployment from this
    point forward is the same as for OpenShift.

    ansible-playbook playbooks/infra/deploy.yaml

>   If you see an error relating to `../../../env/{{ ansible_env.IM_PARAMETER_FILE }}`
    you've probably not sourced your setenv file or provided a value
    for the `IM_PARAMETER_FILE` environment variable
    
>   If you see the error `User "admin" cannot create imagestreams.image.openshift.io`
    in the `Deploy Keycloak Image Stream` playbook task you're probably using
    **MiniShift** but you've not run the MiniShift preparation playbook, which you
    have to do before running any other playbooks.
    See the **Minishift considerations** section at the end of this document.

## Deploying the Squonk CI/CD components
If you want the cluster to also act as the source of images for continuous
builds driven by changes to the Squonk GitHub repository you should deploy
the Squonk CI/CD project and its Source-To-Image (s2i) objects: -

    ansible-playbook  playbooks/squonk-cicd/deploy.yaml

Before doing this you will meed to define values for the following
environment variables, ideally putting them in your vault-protected
site script in the `env` directory: -

-   IM_SQUONK_CICD_TRIGGER_SECRET
-   IM_SQUONK_CICD_CXN_MAVEN_USER
-   IM_SQUONK_CICD_CXN_MAVEN_PASSWORD
 
The `IM_SQUONK_CICD_TRIGGER_SECRET` is used in your GitHub **Payload URL**.
`<secret>` field. The payload URL is returned when you _describe_ the
main Squonk build configuration, e.g.: -

    oc project squonk-cicd
    oc describe bc squonk-build
    
Where you will see something like the following (wrapped for readability): -

    Webhook GitHub:
	URL: https://prod.openrisknet.org:443/apis/build.openshift.io/v1
	        /namespaces/squonk-cicd/buildconfigs/squonk-build/webhooks
	        /<secret>/github

## Deploying the key application components
>   At this stage you might need to enable the secure route to Keycloak
    by setting the corresponding `kubernetes.io/tls-acme:` annotation of the
    route to `'true'`.

You can run Squonk's playbooks from this directory with the commands: -

    ansible-playbook playbooks/squonk/deploy.yaml
    ansible-playbook playbooks/squonk-chemcentral/deploy.yaml

>   If you want the Squonk deployment to be driven by the CI/CD project
    you should set the cicd variable when deploying Squonk by adding the
    following to the command-line `-e oc_squonk_image_source_cicd=yes`

## Adding users
>   At this stage you might need to enable the secure routes to Squonk
    by setting the corresponding `kubernetes.io/tls-acme:` annotation of the
    route to `'true'`.

You can add users from a text file (that contains one user and space-separated
password per line) 'after-the-fact' by defining the `user_file` playbook
variable and then limiting the deployment to just the tasks relating to
_keycloak users_ with the following: -

    ansible-playbook -e "users_file=example.user" -t keycloak-users \
        playbooks/squonk/deploy.yaml

>   `example.user` is a demo file. You can use the file `users.user`
    to safely add your own users. It is prevented from being committed to
    Git as it's listed in the project's `.gitignore` file.

## Posting Squonk pipelines
The playbooks for posting pipelines to Squonk are in this project.
There is one role, shared with each set of pipelines that can be posted.

>   The details of each postable pipeline is defined in the `default/main.yaml`
    file of the `squonk-pipelines` role. There you'll find a map detailing
    image and tags names of each pipelines image-posting container.

Deployment of the 'public' pipelines is achieved with the following
Ansible play: -

    ansible-playbook playbooks/squonk-pipelines/deploy-pipelines.yaml

Essentially there is one role and a playbook for each set of pipelines we
expect to deploy with it. The role simply needs a poster container **image**
and a **tag**, which are defined in `roles/squonk-pipelines/defaults/main.yaml`

If you add a new set of pipelines the expectation is that you'd add a new
playbook (in `playbooks/squonk-pipelines`) and adjust the role's `sd_poster`
variable to include the container image and tag for your new pipelines.

### Validating (testing) pipelines
Once pipelines have been deployed you can validate their basic operation
using the _validate pipelines_ playbook. It relies on the built-in `user1`
user and creates a Keycloak login for the user and then runs some example
pipelines before removing the user.

    ansible-playbook playbooks/squonk-pipelines/validate-pipelines.yaml

>   If a validation user already exists (**user1**), you will need to
    provide the validation playbook with its password by appending
    `-e user1_password=<ThePassword>` to the play execution.

Remember that this playbook creates jobs on the server, so running it
on an _active_ deployment is unwise. Use it as a sanity check when it's safe
or after initial deployment to quickly verify the installation.
 
If you get into trouble with failed tests a convenient _cleanup_
playbook also exists. It also creates the keycloak login for the `user1`
user (if required), cleans up and then removes the user
(if it didn't already exist).

Use this playbook with caution as it removes all the jobs with a status of
`RESULTS_READY` and `ERROR`: -

    ansible-playbook playbooks/squonk-pipelines/validate-cleanup.yaml

## Populating the ChemCentral database
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
        -e oc_db=mydb \
        -e oc_db_user=me \
        -e oc_db_namespace=myproject
        
    ansible-playbook playbooks/infra/delete-user-db.yaml \
        -e oc_db=mydb \
        -e oc_db_user=me

The result will be secrets created in your project
(`myproject` in the above example) containing  the database credentials that
your application can use. This process is used during the Squonk 
deployment to create the database that it uses.

## Displaying playbook variables
We have a number of playbooks and a large number of variables control these
playbooks. As variables are not located in a single location it's often difficult
to know what variables exist and what values they'll have when a playbook is
executed.

As knowing a variable's value is likely to be important when you run a
playbook there is a way to display them (using a playbook).

There are two types of variable: -

1.  Ansible variables
2.  Environment variables

All our Ansible variables being `oc_` and all our environment variables begin
`IM_`. Each of our roles provides a `display-variables.yaml` play that displays
these variables and their values. It can be run with a corresponding playbook.
For example, to display all the variables available to the **infra** role you
can run the following: -

    ansible-playbook playbooks/infra/display-variables.yaml

The playbook spends a few moments gathering the variables that it has access
to and then prints them and their values. At the end of the plybook's output
you will see the environment and Ansible variables printed for you, looking
a bit like this: -

    TASK [infra : Display environment variables] ******************************
    ok: [127.0.0.1] => {
        "im_vars": {
            "IM_PARAMETER_FILE": "params-minishift.yaml"
        }
    }
    
    TASK [infra : Display Ansible variables] **********************************
    ok: [127.0.0.1] => {
        "oc_vars": {
            "oc_admin": "admin", 
            "oc_cc_infra_volume_type": "minishift", 
            "oc_cc_loader_cpu_request": "100m", 
            "oc_cc_loader_mem_request": "100Mi", 
            "oc_cc_postgresql_cpu_request": "100m", 
            "oc_cc_postgresql_mem_request": "100Mi", 
            ...
        }
    }

As well as a playbook for **infra** there are separate playbooks to display
the variables available for the **squonk**, **squonk-chemcentral**,
**squonk-cicd** and **squonk-pipelines** roles: -

    ansible-playbook playbooks/squonk/display-variables.yaml
    ansible-playbook playbooks/squonk-chemcentral/display-variables.yaml
    ansible-playbook playbooks/squonk-cicd/display-variables.yaml
    ansible-playbook playbooks/squonk-pipelines/display-variables.yaml

## Minishift considerations
While it's a work-in-progress, support for some versions of Minishift is
available. We tend to follow recent Minishift releases, at the moment we've
tested with: -

-   OpenShift 3.11.0 on Minishift 1.34.0
-   VirtualBox 5.2.20 and 6.0.8 (OSX)

On OSX, using VirtualBox, you can start a suitable Minishift
with something like: -

    minishift start --cpus 4 --memory 8GB --disk-size 80GB \
        --openshift-version 3.11.0 --vm-driver virtualbox

You need the `oc` executable on your path. For MiniShift do this:

    eval $(minishift oc-env)
 
You need to setup a suitable `setenv.sh` (and source it).
`openshift/env/setenv-minishift.sh` should work in most cases.
Then run the `minishift` playbook to prepare the cluster **before** running
the above Squonk plays.

From this directory, run: -

    source ../env/setenv-minishift.sh
    ansible-playbook playbooks/minishift/prepare.yaml

>   The MiniShift installation does not use trusted certificates so
    you need to instruct your browser to ignore the security concerns
    that this causes.
