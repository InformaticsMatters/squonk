# Squonk Ansible OpenShift Deployment
If you provide the vault password in the file `vault-pass.txt`
you can run the playbook from this directory with the command: -

    ansible-playbook site.yaml --vault-password-file vault-pass.txt

>   The vault password file `vault-pass.txt` is listed in `.gitignore`
    so it shouldn't be vulnerable to an accidental commit.

>   You can also set ANSIBLE_VAULT_PASSWORD_FILE environment variable,
    e.g. `ANSIBLE_VAULT_PASSWORD_FILE=vault-pass.txt` and Ansible will
    automatically search for the password in that file.

Alternatively, to avoid placing the password in a file you can provide the
vault password by forcing Ansible to prompt for the vault password like this...

    ansible-playbook site.yaml --ask-vault-pass

## Prerequisites
Before running the playbook: -

1.  You're on the bastion node
1.  You have installed Ansible (any version from 2.5)
1.  The `oc` command-set is available to you as a user
1.  An OpenShift cluster has been installed
1.  There is an `admin` user known to the cluster
1.  There is a `developer` user known to the cluster
1.  You have created a `vault-pass.txt` file in this directory
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
