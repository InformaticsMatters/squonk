# Instructions for setting up an Ubuntu server for Squonk

## Server setup

### Install the goodies

This is based on Ubuntu Xenial server running on [Scaleway](https://cloud.scaleway.com). 
This was tested using a X64-15GB server and a C2M 8-core 16GB server.
Depending on your privileges you may need to sudo some comamnds.

```sh
# apt-get update
# apt-get install -y git curl jq apt-transport-https ca-certificates software-properties-common openjdk-8-jdk
# curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
# add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
# apt-get update
# apt-cache search docker-ce
# apt-get install -y docker-ce
```

The above `search` avoid the following error:

    # apt-get install -y docker-ce
    Reading package lists... Done
    Building dependency tree       
    Reading state information... Done
    E: Unable to locate package docker-ce
    
To test docker:

```sh
docker run hello-world
```

Now Install Docker Compose

```sh
curl -L https://github.com/docker/compose/releases/download/1.14.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
```

And command line completions

```sh
# curl -L https://raw.githubusercontent.com/docker/docker-ce/master/components/cli/contrib/completion/bash/docker > /etc/bash_completion.d/docker
# curl -L https://raw.githubusercontent.com/docker/compose/master/contrib/completion/bash/docker-compose -o /etc/bash_completion.d/docker-compose
```

Check permissions on the docker-compose executable found in /usr/local/bin. If not set to be executable do this:

```sh
# chmod 755 /usr/local/bin/docker-compose
```

Install certbot:

```sh
# add-apt-repository -y ppa:certbot/certbot
# apt-get update
# apt-get install -y certbot
```

Alternatively if there is no packaged version of cerbot for your system you can use certbot-auto.
See [here](https://certbot.eff.org/all-instructions/#ubuntu-other-nginx) for more.

### Setup the SSL certificates

This is only necessary if you are running a public site. For testing you can use self-signed certificates 
(see below for details).

This approach uses certificates from Let's Encrypt wich are trusted by most browsers and are free.
It partly follows a description found [here](https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-ubuntu-16-04).


Generate certificate. If your not already registered with Let's Encrypt then do this and you need to answer various questions:

```sh
# certbot certonly --standalone -d squonk.informaticsmatters.com
```

If you are already registered you can avoid this using:

```ssh
# certbot certonly --standalone -n -m your@email.address --agree-tos -d squonk.informaticsmatters.com 

```
(replace squonk.informaticsmatters.com with your actual domain and your@email.address with the email you are registed as).
The certs will be in /etc/letsencrypt/live/<domain name>

Generate Strong Diffie-Hellman Group:

>   The Diffie–Hellman key exchange method allows two parties
    that have no prior knowledge of each other to jointly
    establish a shared secret key over an insecure channel.

```sh
# openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048
```
This takes a few mins.

### Create the Squonk user
Adding it to the `docker` and `sudo` group...

```sh
# useradd -m squonk -s /bin/bash
# gpasswd -a squonk docker
# gpasswd -a squonk sudo
```

### SSH and a key-pair
Create a key pair (on your client):

```
$ ssh-keygen -t rsa -b 4096
Generating public/private rsa key pair.
Enter file in which to save the key (/Users/abc/.ssh/id_rsa): /Users/abc/.ssh/squonk_rsa
Enter passphrase (empty for no passphrase): 
Enter same passphrase again: 
Your identification has been saved in /Users/abc/.ssh/squonk_rsa.
Your public key has been saved in /Users/abc/.ssh/squonk_rsa.pub.
The key fingerprint is:
SHA256:7qxB0Cd5dx9IYdfNGpKVtYaNiWAMtIUwKA4P5GWdsTY abc@Dallas.local
The key's randomart image is:
+---[RSA 4096]----+
|.. oo+=o++  ++o=o|
|+.o. +oo+..o+oB =|
|o+. .E+.o ..o=.* |
| .. ...+ . . .o. |
|      . S     .  |
|     . .         |
|      . .        |
|       +         |
|      ..o        |
+----[SHA256]-----+
```

Create or append to the squonk user's `.ssh/authorized_keys` file on the new server.
Here you're copying the public key content form your client.
Remember to set the permisssions of this file to 600:

    $ chmod 600 ~/.ssh/authorized_keys

Test you can login as that user with ssh.
Adjust sshd settings in `/etc/ssh/sshd_config` according to your needs.

## Squonk setup

Login as squonk user
Make sure JAVA_HOME is set using something like this:

```sh
$ export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

>   Best to put this in your `$HOME/.profile`.

```sh
$ mkdir git
$ cd git
$ git clone https://github.com/InformaticsMatters/squonk.git
$ git clone https://github.com/InformaticsMatters/pipelines.git
$ cd pipelines
$ ./copy.dirs.sh
$ cd ..
```

Now pull the docker images. This will take a few mins.
```sh
$ cd squonk/docker/deploy
$ ./images-pull-squonk.sh
$ ./images-pull-extra.sh
```

### Copy the keys

```sh
$ mkdir -p images/nginx/certs/squonk
$ cp /etc/ssl/certs/dhparam.pem images/nginx/certs/squonk/
$ sudo cp /etc/letsencrypt/live/squonk.informaticsmatters.com/fullchain.pem images/nginx/certs/squonk/
$ sudo cp /etc/letsencrypt/live/squonk.informaticsmatters.com/privkey.pem images/nginx/certs/squonk/
```

>   Remember to use the URL you used when setting up certbot.

Alternatively, if you are running locally and don't need trusted certificates you can use self signed ones.
The browser will complain and you will need to accept you are using non-trusted certificates.
Do this using something like:

```sh
$ mkdir -p images/nginx/certs/squonk
$ openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout images/nginx/certs/squonk/privkey.pem -out images/nginx/certs/squonk/fullchain.pem
```

Whether you're using official certificates or untrusted ones
you should also create the `dhparam.pem` file like this:

```sh
$ openssl dhparam -out images/nginx/certs/squonk/dhparam.pem 2048
```

### Copy license files

Currently this involves ChemAxon and CPSign licenses. Copy them to data/licenses. 

-   `license.cxl` is the ChemAxon licence file
-   ``

### Setup gradle properties
Before starting you need to provide details of the ChemAxon maven rpository.
This goes into Squonk's `~/.gradle/gradle.properties` file and consists of
(`cnx-stuff.txt`):

```
cxnMavenUser=<USER-EMAIL>
cxnMavenPassword=<ENCYPTED-USER-PASSWORD>
cxnMavenRepositoryUrl=https://hub.chemaxon.com/artifactory/libs-release
```

If you don't, you're likely to see the following error
running `./containers-setup-app.sh` later in these notes:

```
A problem occurred evaluating root project 'components'.
> Could not get unknown property 'cxnMavenRepositoryUrl' for object
       of type org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository.
```
    
### Configure and run
From within the `~/git/squonk/docker/deploy` directory...

```sh
$ cp setenv-default.sh setenv.sh
```

Now edit `setenv.sh` to set passwords etc.
You MUST set the value of PUBLIC_HOST to the external FQDN of your server
(or the IP address of the docker bridge network if you are running locally).
You need to define the type of environment you are wanting to deploy. The default is `dev` but for a
public site your probably want to change this to `basic`. Soo the file for details.

```sh 
$ source setenv.sh
```

>   You could add `source ~/git/squonk/docker/deploy/setenv.sh` to squonk's `.profile`.

```
$ ./containers-setup-core.sh
```

This will have started and configured the following services (container images):

-   keycloak (jboss/keycloak-postgres) - only present if running in basic or site mode
-   rabbitmq (rabbitmq)
-   postgres (informaticsmatters/rdkit_cartridge)

```
$ ./containers-setup-app.sh
```

This will have started the Squonk containers and we are ready to run. Access Squonk from the 
URL that is reported by the script.

This should start the following services (containers). The xwiki
is normally only used on the production site.

-   nginx (nginx) - not present in dev mode
-   portal (squonk/portal)
-   coreservices (squonk/coreservices)
-   cellexecutor (squonk/cellexecutor)
-   chemservices (squonk/chemservices)
-   keycloak (jboss/keycloak-postgres) - not present in dev mode
-   rabbitmq (rabbitmq)
-   postgres (informaticsmatters/rdkit_cartridge)

Two busybox containers will have been started and stopped (these provide
synchronisation between the services). 
One squonk/flyway container will have been started and stopped (this updates the Squonk database schema if 
needed).

>   Due to a glitch that is still to be resolved the nginx container takes
    some time to start. Monitor the status using `docker-compose ps`.
    You might even need to do a `docker-compose start nginx` if that container fails to start. 

Check that the site is reachable and forwards http traffic to https.

Check the SSL status using Qualys SSL Labs Report by opening this in web browser.
https://www.ssllabs.com/ssltest/analyze.html?d=squonk.informaticsmatters.com


You can login using one of the built in accounts, user1 and user2, which have passwords the same as the username.

>   NOTE: to make the site safe you should delete these accounts or change
    the passwords uing the admin console which will be found at a url like this:
    `https://squonk.informaticsmatters.com/auth`. Log in as the admin user
    using the `KEYCLOAK_USER` username and `KEYCLOAK_PASSWORD` password that
    you defined in `setenv.sh`.

## Certificate renewal

Let's Encrypt certificates only last for 90 days. if running a permanent server you should automate renewing them.
certbot runs as root so switch to root and create a shell script like this, adjusting the location of DEST_DIR and
where the certificates are found as required:

```sh
#!/bin/sh

DEST_DIR=/home/squonk/git/squonk/docker/deploy/images/nginx/certs/squonk/

cp /etc/letsencrypt/live/squonk.informaticsmatters.com/fullchain.pem $DEST_DIR
cp /etc/letsencrypt/live/squonk.informaticsmatters.com/privkey.pem $DEST_DIR

chown squonk.squonk $DEST_DIR/fullchain.pem
chown squonk.squonk $DEST_DIR/privkey.pem
```
Make the file executable:

```sh
chmod +x copy-certs.sh
```

Login edit the crontab using `crontab -e` and add this like to the end:

```
30 3 * * * /usr/bin/certbot renew --quiet --renew-hook "/root/copy-certs.sh"
```
This will renew any expired certificates at 3:30 am, every day, and if renewed copy them to the right location 
where the Nginx server can see them.

To renew certificate manually do this:

```sh
certbot renew
```
And then copy the certs as above.
