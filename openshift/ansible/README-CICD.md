# Squonk Ansible OpenShift Deployment for CI/CD

## Prerequisites
Before running the playbooks: -

1.  You're on the bastion node, or you are on a node with...
    1.  Ansible installed (any version from 2.5)
    1.  The `oc` command-set is available to you as a user
1.  An OpenShift cluster has been installed
1.  There is an `admin` user known to the cluster
1.  There is a `developer` user known to the cluster
1.  You have setup your own `setenv.sh` (typically in `openshift/env`),
    created a suitable `params` file and you have run `source setenv.sh`.

The CI/CD deployment requires a number of additional environment variables,
normally defined in your `setenv.sh` script: -

1.  `IM_SQUONK_CICD_TRIGGER_SECRET`
1.  `IM_SQUONK_CICD_CXN_MAVEN_USER`
1.  `IM_SQUONK_CICD_CXN_MAVEN_PASSWORD`

## Deploying the CI/CD components
You can run the Squonk CI/CD playbooks from this
directory with the commands: -

    ansible-playbook playbooks/squonk-cicd/deploy.yaml

>   Remember to first `source` an appropriately crafted
    `../env/setenv.sh` script first!

>   If you see an error relating to `../../../env/{{ ansible_env.IM_PARAMETER_FILE }}`
    you've probably not sourced your setenv file or provided a value
    for the `IM_PARAMETER_FILE` environment variable

>   If you see the error `User "admin" cannot list serviceaccounts`
    you're probably using **MiniShift** but you've not run the MiniShift
    preparation playbook, which you have to do before running any other
    playbooks.

## Removing the CI/CD components
Remove the CI/CD deployment with the following playbook...

    ansible-playbook playbooks/squonk-cicd/undeploy.yaml