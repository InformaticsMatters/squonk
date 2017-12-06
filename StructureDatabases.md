# Database loaders

Squonk has a chemical database powered by the RDKit cartridge.
To use this you need to load datasets into database.
Currently there is support for
* eMolecules (bulidng blocks and screening compounds)
* ChEMBL (structures only)
* DrugBank (this is not loaded by default for licensing reasons)
* PDB ligands

The code and configuration for the loaders is in the rdkit-lib module.
At some stage it may be broken out into a separate module.

## Loading data

### Start the Postgres database

Loading requires the postgres database to be running and port 5432 to be exposed.
Normally this port is not exposed.
Move into the docker/deploy dir and start the postgres database

```sh
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d postgres
```

### Configuring loaders

The configuration is contained in a file named components/rdkit-lib/rdkit_loader.properties
You can create this by copying components/rdkit-lib/rdkit_loader.properties.template and editing accordingly.
Download the necessary datafiles and place in data/testFiles as described in the configuration file.

The eMolecules loader has one section tha handles both the building blocks and the screening compounds datasets.
Edit according to the one you are currently loaded. You will want to eventually load both. 

For testing set the loadOnly property to restrict the number of structures to load. Reset this to zero to load the 
entire dataset.

### Running a loader

```sh
./gradlew --daemon -PmainClass=org.squonk.rdkit.db.loaders.EMoleculesSmilesLoader rdkit-lib:execute
```

Adjust the loader name accordingly. Options are:
* org.squonk.rdkit.db.loaders.EMoleculesSmilesLoader - for eMolecules building blocks and screening compounds
* org.squonk.rdkit.db.loaders.DrugBankSdfLoader - for DrugBank
* org.squonk.rdkit.db.loaders.ChemblSdfLoader - For ChEMBL
* org.squonk.rdkit.db.loaders.PdbLigandSdfLoader - for ligands from PDB

Note that loading large datasets like eMolecules screening compounds takes several days.

Note that some loaders require RDKit and its Java bindings to be present. One way to do this is to do the
loading from within the informaticsmatters/rdkit_java container. To do this (assumes the postgres container is 
already running as described above):

```sh
cd $SQUONK_HOME # root of squonk git repo
docker run -it --name rdkitloader --network deploy_squonk_back -v $PWD:/squonk -w /squonk informaticsmatters/rdkit_java:Release_2017_03_1 bash
# now you are in the container do this
cd docker/deploy/
source setenv.sh
cd ../../components
./gradlew --daemon -PmainClass=org.squonk.rdkit.db.loaders.PdbLigandSdfLoader rdkit-lib:execute
```

Note that this gives the container the name of rdkitloader so that it can be reused for efficiency.
To reuse it do:

```sh
docker start -i rdkitloader
```

Loading these datasets can takes hours or days depending on their size. To prevent lost connections to the server terminating 
the load you might want to look into doing the loading using a the 
[linux screen facility](https://www.gnu.org/software/screen/manual/screen.html). 


### Configuring the search service

The searchsearvice needs to know what database tables have been loaded.
This is currently done using the STRUCTURE_DATABASE_TABLES environment variable
that needs to be passes to the chemservices container. To do this set this variable
in your docker/deploy/setenv.sh file. The value must be a colon separated list of table names
with no spaces or other characters allowed. For instance:

```
export STRUCTURE_DATABASE_TABLES=emolecules_order_sc:emolecules_order_bb:chembl_23:pdb_ligand
```


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

