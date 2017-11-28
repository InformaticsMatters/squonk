Basic instructions for Squonk install using Keycloak.
This is work in progress and needs to be further streamlined.
The idea is to allow the name of the *-infra and the squonk projects to be changed
(and for them to be the same). The configuration is controlled by the contents of
the `setenv.sh` file.

Before you run this you must create admin ($OC_ADMIN_USER) and squonk ($OC_USER) accounts and give the admin user
cluster-admin role:
`oc adm policy add-cluster-role-to-user cluster-admin admin`

You must also deploy the xpaas image streams:
`oc create -f https://raw.githubusercontent.com/openshift/openshift-ansible/master/roles/openshift_examples/files/examples/v3.6/xpaas-streams/jboss-image-streams.json -n openshift`

1. Now we are ready to start deploying.
1.1 Create/edit setenv.sh
1.2 `source setenv.sh`

2. Create the certificates used by Keycloak. The certs dn keystores are protected by a singel password
that is spacified as the $OC_CERTS_PASSWORD variable.
2.1 `./certs-create.sh`

3. Create projects as the $OC_ADMIN_USER user:
3.1. `oc new-project $OC_PROJECT`
3.2. `oc new-project $OC_INFRA_PROJECT`

2. Deploy PostgreSQL and Keycloak to the *-infra project:
2.1. `./sso-env-deploy.sh`
2.2. `./sso-secrets-deploy.sh`
2.3. `./sso-deploy.sh`
To get postgres running in some environments would might need to do:
```
minishift ssh
sudo chmod 777 /mnt/sda1/var/lib/minishift/openshift.local.pv/pv0091
```
(lookup the appropriate PV to fix)

You may need to run this to fix a bug that prevents Keycloak from starting:
`oc volume dc/sso --add --claim-size 512M --mount-path /opt/eap/standalone/configuration/standalone_xml_history --name standalone-xml-history`


3. Go to Keycloak and set up the ${OC_SQUONK_APP} client in the appropriate realm.
The admin password will be found in the `sso` secret.
Specify the appropriate URL as the Root URL, something like `https://squonk-notebook.192.168.42.131.nip.io/portal

- set the access type to confidential
- check the Redirect URL, Admin URL and Web Origins.
- add http variants for Redirect URL and Web Origins
- make a note of the client secret (credentials tab)
- create a `standard-user` role and set as default role
- create `standard-user` role and specify as a default role
- create user(s), specify passwords (Credentials tab) and confirm they get the `standard-user` role 

4.1 Edit setenv.sh and set the `KEYCLOAK_SECRET` variable to the client secret
4.2 `source setenv.sh`

5. Deploy Squonk to the squonk project:
5.1. `./squonk-db-deploy.sh`
5.2. `./squonk-infra-deploy.sh`
5.3. `./squonk-app-deploy.sh`