# Database loaders

Squonk has a chemical database powered by the RDKit cartridge.
To use this you need to load datasets into database.
Currently there is support for
* eMolecules (bulidng blocks and screening compounds)
* ChEMBL (structures only)
* DrugBank (this is not loaded by default for licensing reasons)
* PDB ligands
* Chemspace
* Molport
* LTKB dataset from FDA

The code and configuration for the loaders is in the rdkit-databases module.
At some stage it may be broken out into a separate module.

## Loading data

### Start the Postgres database

Loading requires the chemcentral postgres database to be running and port 5432 to be exposed.
This port may not be exposed by default.
This may be a separate postgres database to the the one that stores the Squonk user data as it also runs the RDKit cartridge.
Move into the docker/deploy dir and start the chemcentral postgres database. An example docker-compose.yml file can be found 
in the rdkit-lib sub-project.

```
docker-compose up -d
```

### Configuring loaders

Loaders are run using the squonk/chemcentral-loader docker image.
Each loader is pre-configured with sensible defaults, but you can override these using environment variables:

* CHEMCENTRAL_HOST - the hostname where postgres is running (default: localhost)
* CHEMCENTRAL_PORT - the port where postgres is running (default: 5432)
* CHEMCENTRAL_DATABASE - the name of the database (default: chemcentral)
* CHEMCENTRAL_USER - the database username (default: chemcentral)
* CHEMCENTRAL_PASSWORD  - the password for the database user (default: chemcentral)
* CHEMCENTRAL_SCHEMA - the postgres schema to use (default: vendordbs)
* CHEMCENTRAL_TABLE - the name of the main database table (default value for each loader e.g. the Chemspace loader uses a table name of chemspace)
* CHEMCENTRAL_LOADER_FILE - the file name containing the data to load (default value for each loader e.g. the Chemspace loader uses a default of /rdkit/chemspace.sdf.gz)
* CHEMCENTRAL_LOADER_DIR - the directory with the files to load (e.g. for the Molport loader)
* CHEMCENTRAL_LOADER_PATTERN - regex for the files to load where there are multiple files (e.g. for Molport)
* CHEMCENTRAL_LIMIT - the number of records to load for each file (default value is 0 which means load all records)
* CHEMCENTRAL_REPORTING_CHUNK - the frequency to report loading (default appropriate to the typical size of the dataset)
* CHEMCENTRAL_ALIAS - an optional alias for the table so that it can be accessed using a symbolic name e.g. chembl_latest

For testing set the CHEMCENTRAL_LIMIT property to restrict the number of structures to load. Reset this to zero to load the 
entire dataset.

### Running a loader

Run the loader with a command like this:

```
docker run -it --rm -v $HOME/Downloads/2021-02_Chemspace_Building_Blocks_Make-on-Demand_20M_SMILES.smiles.gz:/rdkit/chemspace.smiles.gz:ro\
  -e CHEMCENTRAL_HOST=172.17.0.1\
  -e CHEMCENTRAL_LIMIT=200000\
  -e CHEMCENTRAL_USER=squonk\
  -e CHEMCENTRAL_PASSWORD=squonk\
  squonk/chemcentral-loader:latest\
  org.squonk.rdkit.db.loaders.ChemspaceSmilesLoader
```

Adjust the value of the CHEMCENTRAL_HOST variable to where postgres is running (possibly the Docker gateway address).

The final parameter is the classname of the loader to use. This is passed as an argument to the container's entrypoint which
will be something like this `/rdkit-databases-0.2-SNAPSHOT/bin/rdkit-databases`. 

Adjust the volume mount that specifies the file to load. In this example we mount it to the default name expected by the loader.
If using a different name also specify the CHEMCENTRAL_LOADER_FILE environment variable to point to the absolute file name that is mounted.

For loaders such as Molport that have multiple input files instead of CHEMCENTRAL_LOADER_FILE you can specify  
CHEMCENTRAL_LOADER_DIR (the directory in the container with the files) and
CHEMCENTRAL_LOADER_PATTERN (a regex that defines the filenames to load). To use the defaults run with a command like this:

```
docker run -it --rm -v $HOME/tmp/molport/2018-08/All\ Stock\ Compounds/SMILES:/rdkit/molport:ro,Z\
  -e CHEMCENTRAL_HOST=172.17.0.1 -e CHEMCENTRAL_LIMIT=10000\
  squonk/chemcentral-loader:latest\
  org.squonk.rdkit.db.loaders.MolportSmilesLoader
```

Remove the definition of the CHEMCENTRAL_LIMIT environment variable to load the entire dataset. 

Adjust the loader name (the last argument) accordingly. Options are:
* org.squonk.rdkit.db.loaders.EMoleculesBBSmilesLoader - for eMolecules building blocks (http://emolecules.com/info/plus/download-database)
* org.squonk.rdkit.db.loaders.EMoleculesSCSmilesLoader - for eMolecules screening compounds (http://emolecules.com/info/plus/download-database)
* org.squonk.rdkit.db.loaders.DrugBankSdfLoader - for DrugBank (http://www.drugbank.ca/downloads NOTE: DrugBank is no longer free to use. Look at the licensing before using it. It is not loaded into the public Squonk site)
* org.squonk.rdkit.db.loaders.ChemblSdfLoader - For ChEMBL (ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/)
* org.squonk.rdkit.db.loaders.PdbLigandSdfLoader - for ligands from PDB (http://ligand-expo.rcsb.org/ld-download.html. The file you want is in the "Chemical component coordinate data files" section and called all-sdf.sdf.gz. Fetch it with something like: 'wget http://ligand-expo.rcsb.org/dictionaries/all-sdf.sdf.gz')
* org.squonk.rdkit.db.loaders.ChemspaceSmilesLoader - Chemspace (Obtain this file directly from the ChemSpace people)
* org.squonk.rdkit.db.loaders.MolportSmilesLoader - Molport in smiles format (https://www.molport.com/shop/database-download)
* org.squonk.rdkit.db.loaders.LTKBLoader - LTKB dataset (see https://www.fda.gov/ScienceResearch/BioinformaticsTools/LiverToxicityKnowledgeBase/ucm2024036.htm)

Source code for these loaders can be found [here](https://github.com/InformaticsMatters/squonk/tree/master/components/rdkit-databases/src/main/groovy/org/squonk/rdkit/db/loaders).

Note that loading large datasets like eMolecules screening compounds or Molport takes several days.
To prevent lost connections to the server terminating the load you might want to look into doing the loading using a the 
[linux screen facility](https://www.gnu.org/software/screen/manual/screen.html).


### Updating the search service

The search service, coreservices and the Squonk portal application read the definition of which tables have been loaded 
when they start and will not get updated if additional tables are loaded. To pick up updated information there services 
must be restarted. We hope to avoid the need for this in the future.


## SQL

The SQL used to create the indexes looks like this (for chembl_23):

```
DROP TABLE IF EXISTS vendordbs.chembl_23_molfps;
SELECT * INTO vendordbs.chembl_23_molfps FROM (SELECT id,mol_from_ctab(structure::cstring) m FROM vendordbs.chembl_23) tmp where m IS NOT NULL;
ALTER TABLE vendordbs.chembl_23_molfps ADD PRIMARY KEY (id);
ALTER TABLE vendordbs.chembl_23_molfps ADD CONSTRAINT fk_chembl_23_molfps_id FOREIGN KEY (id) REFERENCES vendordbs.chembl_23 (id);
CREATE INDEX idx_chembl_23_molfps_m ON vendordbs.chembl_23_molfps USING gist(m);
ALTER TABLE vendordbs.chembl_23_molfps DROP COLUMN IF EXISTS rdk CASCADE;
ALTER TABLE vendordbs.chembl_23_molfps ADD COLUMN rdk bfp;
UPDATE vendordbs.chembl_23_molfps SET rdk = rdkit_fp(m);
CREATE INDEX idx_chembl_23_molfps_rdk ON vendordbs.chembl_23_molfps USING gist(rdk);
ALTER TABLE vendordbs.chembl_23_molfps DROP COLUMN IF EXISTS mfp2 CASCADE;
ALTER TABLE vendordbs.chembl_23_molfps ADD COLUMN mfp2 bfp;
UPDATE vendordbs.chembl_23_molfps SET mfp2 = morganbv_fp(m,2);
CREATE INDEX idx_chembl_23_molfps_mfp2 ON vendordbs.chembl_23_molfps USING gist(mfp2);
ALTER TABLE vendordbs.chembl_23_molfps DROP COLUMN IF EXISTS ffp2 CASCADE;
ALTER TABLE vendordbs.chembl_23_molfps ADD COLUMN ffp2 bfp;
UPDATE vendordbs.chembl_23_molfps SET ffp2 = featmorganbv_fp(m,2);
CREATE INDEX idx_chembl_23_molfps_ffp2 ON vendordbs.chembl_23_molfps USING gist(ffp2);
```

To test substructure search:
```
select count(*) from vendordbs.chembl_23_molfps WHERE m@>'c1cccc2c1CNCCN2';
```

To test similarity search:
```
select count(*) from vendordbs.chembl_23_molfps WHERE mfp2%morganbv_fp('CN1CCc2cccc3c2[C@H]1Cc1ccc(CO)c(O)c1-3');
```

