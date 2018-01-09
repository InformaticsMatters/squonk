# Database loaders

Squonk has a chemical database powered by the RDkit cartridge.
To use this you need to load datasets into database.
Currently there is support for
* eMolecules (bulidng blocks and screening compounds)
* ChEMBL (structures only)
* DrugBank (this is not loaded by default for licensing reasons)
* PDB ligands
* Chemspace

The code for the loaders is in the rdkit-databases module.

## Loading data

### Start the Postgres database

Loading requires the chemcentral postgres database to be running and port 5432 to be exposed.
This port may not be exposed by default.
This is a separate postgres database to the the one that stores the Squonk user data as it also runs the RDKit cartridge.
Move into the docker/deploy dir and start the chemcentral postgres database. An example docker-compose.yml file can be found 
in the rdkit-lib sub-project.

```
docker-compose up -d
```

### Configuring loaders

Loaders are run using the squonk/chemcentral-loader:openshift docker image.
Each loader is pre-configured with sensible defaults, but you can override these using environment variables:

* CHEMCENTRAL_HOST - the hostname where postgres is running (default: localhost)
* CHEMCENTRAL_PORT - the port where postgres is running (default: 5432)
* CHEMCENTRAL_DATABASE - the name of the database (default: chemcentral)
* CHEMCENTRAL_USER - the database username (default: chemcentral)
* CHEMCENTRAL_PASSWORD  - the password for the database user (default: chemcentral)
* SCHEMA_NAME - the postgres scheam to use (default: vendordbs)
* TABLE_NAME - the name of the main database table (default value for each loader e.g. the Chemspace loader uses a table name of chemspace)
* LOADER_FILE - the file name containing the data to load (default value for each loader e.g. the Chemspace loader uses a default of /rdkit/chemspace.sdf.gz)
* LIMIT - the number of records to load (default value is 0 which meeans load all records)
* REPORTING_CHUNK - the frequency to report loading (default apporpopriate to the typical size of the dataset)


### Running a loader

Run the loader with a command like this:

```
docker run -it --rm -v $HOME/data/structures/chemspace/201711_Chemspace_representative_catalogue_3_54M_sdf.sdf.gz:/rdkit/chemspace.sdf.gz:ro,Z \
  -e CHEMCENTRAL_HOST=172.17.0.1 \
  -e LIMIT=20000 \
  squonk/chemcentral-loader:openshift \
  org.squonk.rdkit.db.loaders.ChemspaceSdfLoader
```

Adjust the value of the CHEMCENTRAL_HOST variable to where postgres is running (possibly the Docker gateway address).

Adjust the volume mount that specifies the file to load. In this example we mount it to the default name expected by the loader.
If using a different name also specify the LOADER_FILE environment variable to point to the absolute file name that is mounted.

Remove the definition of the LIMIT environment variable to load the entire dataset. 

Adjust the loader name (the last argument) accordingly. Options are:
* org.squonk.rdkit.db.loaders.EMoleculesBBSmilesLoader - for eMolecules building blocks (http://emolecules.com/info/plus/download-database)
* org.squonk.rdkit.db.loaders.EMoleculesSCSmilesLoader - for eMolecules screening compounds (http://emolecules.com/info/plus/download-database)
* org.squonk.rdkit.db.loaders.DrugBankSdfLoader - for DrugBank (http://www.drugbank.ca/downloads NOTE: DrugBank is no longer free to use. Look at the licensing before using it. It is not loaded into the public Squonk site)
* org.squonk.rdkit.db.loaders.ChemblSdfLoader - For ChEMBL (ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/)
* org.squonk.rdkit.db.loaders.PdbLigandSdfLoader - for ligands from PDB (http://ligand-expo.rcsb.org/ld-download.html. The file you want is in the "Chemical component coordinate data files" section and called all-sdf.sdf.gz. Fetch it with something like: 'wget http://ligand-expo.rcsb.org/dictionaries/all-sdf.sdf.gz')
* org.squonk.rdkit.db.loaders.ChemspaceSdfLoader - Chemspace (Obtains this file directly from the ChemSpace people) 

Source code for these loaders can be found [here](https://github.com/InformaticsMatters/squonk/tree/openshift/components/rdkit-databaess/src/main/groovy/org/squonk/rdkit/db/loaders).
Currently these are on the openshift branch only, but eventually this will be merged to master.

Note that loading large datasets like eMolecules screening compounds takes several days.
To prevent lost connections to the server terminating the load you might want to look into doing the loading using a the 
[linux screen facility](https://www.gnu.org/software/screen/manual/screen.html). 


### Configuring the search service

The searchsearvice needs to know what database tables have been loaded.
This is currently done using the STRUCTURE_DATABASE_TABLES environment variable
that needs to be passes to the chemservices container. To do this set this variable
in your docker/deply/setenv.sh file. The value must be a colon separated list of table names
with no spaces or other characters allowed. For instance:

```
export STRUCTURE_DATABASE_TABLES=emolecules_order_sc:emolecules_order_bb:chembl_23:pdb_ligand
```


