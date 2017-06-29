# Instructions for setting up an Ubuntu server for Squonk

## Server setup

This is based on Ubuntu Xenial server running on [Scaleway](https://cloud.scaleway.com).

```sh
apt-get update
apt-get install -y git curl jq apt-transport-https ca-certificates software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
apt-key fingerprint 0EBFCD88
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
apt-get update
apt-get install -y docker-ce
```

To test docker:

```sh
docker run hello-world
```

Now Install Docker Compose

```sh
curl -L https://github.com/docker/compose/releases/download/1.14.0/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
```

Create a Squonk user

```sh
useradd -m squonk
gpasswd -a squonk docker
gpasswd -a squonk sudo
```

For that user add public key to $HOME/.ssh/authorized_keys.
Test you can login as that user with ssh.
Adjust sshd settings in /etc/ssh/sshd_config according to your needs

## Squonk setup

Login as squonk user

```sh
mkdir git
cd git
git clone https://github.com/InformaticsMatters/squonk.git
cd squonk/docker/deploy
```








