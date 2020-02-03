# Squonk main repository

![GitHub](https://img.shields.io/github/license/informaticsmatters/squonk)
[![Build Status](https://travis-ci.org/InformaticsMatters/squonk.svg?branch=master)](https://travis-ci.org/InformaticsMatters/squonk)
![Codacy grade](https://img.shields.io/codacy/grade/d7ff748f71f04962b4131975a14864d3)
![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/informaticsmatters/squonk)

This is the main repository for [Squonk], both the **Squonk
Platform** and the **Squonk Computational Notebook**.

Currently the portal application part of the Squonk Computational Notebook
is NOT included as that is in a separate repository that needs to be merged
into this one. Also part of Squonk are components from the [Pipelines repository].

The main source code is in the components directory. To build:

Checkout the [Pipelines repository] project into the same directory that 
contains the squonk project. Then copy the current set of service descriptors
to Squonk (this is needed for some of the tests and for those services to be
present at runtime):

    cd pipelines
    ./copy.dirs.sh

Next you need to setup access to the ChemAxon Maven repository. 
Instructions for this can be found in the [ChemAxon docs],
though they are slightly garbled.

Create or edit the gradle.properties file in your `$HOME/.gradle` directory
and add these 3 properties:

    cxnMavenUser=YOUR_EMAIL
    cxnMavenPassword=YOUR_ACCESS_KEY
    cxnMavenRepositoryUrl=https://hub.chemaxon.com/artifactory/libs-release

Replace `YOUR_EMAIL` and `YOUR_ACCESS_KEY` with the appropriate values.
Instructions for generating these can be found [ChemAxon docs].

Now build squonk by moving into the squonk/components directory and use the 
Gradle build file there. e.g.

    cd ../squonk/components
    ./gradlew build

## RDKit services
For some of the tests and some of the RDKit services you will need RDKit with
Java bindings to be present. See the [RDKit docs] for how to do this. If you
don't have this then make sure the RDBASE environment variable is not set.

## ChemAxon services
Data for the ChemAxon services and tests is located in the project's
`encrypted` directory. These files are installed automatically when the build
executes in [Travis] and the build intelligently detects whether you've
installed the files and, if not, the corresponding tests are ignored.

For your own protection you should install these files as Travis does
so that the full set of tests are executed, but you will need the
_encryption key_.

    export SQUONK_DECRYPTION_KEY="GetTheKey"
    cd components
    ./gradlew installChemaxonLicenseToHome
    ./gradlew installChemaxonLibrary

>   The `installChemaxonLicenseToHome` installs the license file to your
    `$HOME/.chemaxon` directory. If you want to put the file elsewhere you
    can, as long as you set `CHEMAXON_HOME` to the directory you use.

Once you've done this you can benefit from the full set of unit tests.

## The CI/CD process (Travis)
**Do not deploy Squonk from your own (local) builds**. Always use
images built and pushed by the CI/CD (Travis) process. Squonk is automatically
built and (unit) tested using Travis and images pushed to Docker Hub from there.

The Travis-derived builds provide the _single point of truth_.

-   You must not push `latest` or officially tagged images from your
    workstation
-   When deploying always commit your changes (on a branch)
    and rely on images pushed to Docker Hub by the Travis process
    
See the **DEV-PROCESS.md** document for further details.
  
## Contributing
Currently it is not expected that third party developers will find it
easy to contribute to this codebase, but that will change. If you are
interested in using or contributing contact Tim Dudgeon
(tdudgeon \_at\_ informaticsmatters \_dot\_ com).

---

[bugs]: https://github.com/travis-ci/travis.rb/issues/571
[ChemAxon docs]: https://docs.chemaxon.com/display/docs/Public+Repository#PublicRepository-HowtoCongfigureYourProject
[Pipelines repository]: https://github.com/InformaticsMatters/pipelines
[RDKit docs]: http://rdkit.org/docs/Install.html#building-from-source
[Squonk]: (http://squonk.it)
[Travis]: https://travis-ci.org/InformaticsMatters/squonk
[Validate]: https://support.travis-ci.com/hc/en-us/articles/115002904174-Validating-travis-yml-files
