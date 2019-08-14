# Squonk main repository

![GitHub](https://img.shields.io/github/license/informaticsmatters/squonk)
[![Build Status](https://travis-ci.org/InformaticsMatters/squonk.svg?branch=master)](https://travis-ci.org/InformaticsMatters/squonk)
![Codacy grade](https://img.shields.io/codacy/grade/d7ff748f71f04962b4131975a14864d3)
![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/informaticsmatters/squonk)

This is the main repository for [Squonk](http://squonk.it), both the Squonk Platform and the 
Squonk Computational Notebook.

Currently the portal application part of the Squonk Computational Notebook is NOT included as that
is in a separate repository that needs to be merged into this one. Also part of Squonk are components
from the [Pipelines repository](/InformaticsMatters/pipelines).

The main source code is in the components directory. To build:

Checkout the [pipelines](https://github.com/InformaticsMatters/pipelines) project into the same directory that 
contains the squonk project. Then copy the current set of service descriptors to Squonk (this is needed for some of the tests and for those services to be present at runtime):

```sh
cd pipelines
./copy.dirs.sh
```

Next you need to setup access to the ChemAxon Maven repository. 
Instructions for this can be found [here](https://docs.chemaxon.com/display/docs/Public+Repository#PublicRepository-HowtoCongfigureYourProject),
though they are slightly garbled.

Create or edit the gradle.properties file in your $HOME/.gradle directory and add these 3 properties:
```
cxnMavenUser=YOUR_EMAIL
cxnMavenPassword=YOUR_ACCESS_KEY
cxnMavenRepositoryUrl=https://hub.chemaxon.com/artifactory/libs-release
```
Replace YOUR_EMAIL and YOUR_ACCESS_KEY with the appropriate values.
Instructions for generating these can be found 
[here](https://docs.chemaxon.com/display/docs/Public+Repository#PublicRepository-HowtoCongfigureYourProject)

Now build squonk by moving into the squonk/components directory and use the 
Gradle build file there. e.g.

```sh
cd ../squonk/components
./gradlew build
```

For some of the tests and some of the RDKit services you will need RDKit with Java bindings to be present. 
See [here](http://rdkit.org/docs/Install.html#building-from-source) for how to do this.
If you don't have this then make sure the RDBASE environment variable is not set.

For the ChemAxon services and tests you will need a ChemAxon license file named license.cxl.
Place this in your $HOME/.chemaxon directory AND in the docker/deploy/images/chemservices dir in the squonk distro.

If for these reasons you find some of the tests failing try using:

```sh
./gradlew build --continue
```

The deployment parts are found in the docker/deploy directory. NOTE: the deployment will be changing
significantly over the coming months as we move to a Kubernetes/Opeshshift deployment.

Currently it is not expected that third party developers will find it easy to contribute to this codebase,
but that will change. If you are interested in using or contributing contact 
Tim Dudgeon tdudgeon \_at\_ informaticsmatters \_dot\_ com.
