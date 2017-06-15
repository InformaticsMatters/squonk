# Squonk main repository

This is the main repository for [Squonk](http://squonk.it), both the Squonk Platform and the 
Squonk Computational Notebook.

Currently the portal application part of the Squonk Computational Notebook is NOT included as that
is in a separate repository that needs to be merged into this one. Also part of Squonk are components
from the [Pipelines repository](/InformaticsMatters/pipelines).

The main source code is in the components directory. To build move into that directory and use the 
Gradle build file there. e.g.

```sh
cd components
./gradlew build
```

For some of the tests you will need RDKit with Java bindings to be present. See 
[here](http://rdkit.org/docs/Install.html#building-from-source) for how to do this.

For the ChemAxon services you will need a ChemAxon license file.

The deployment parts are found in the docker/deploy directory. NOTE: the deployment will be changing
significantly over the coming months as we move to a Kubernetes/Opeshshift deployment.

Currently it is not expected that third party developers will find it easy to contribute to this codebase,
but that will change. If you are interested in using or contributing contact 
Tim Dudgeon<tdudgeon _at_ informaticsmatters _dot_ com>.
