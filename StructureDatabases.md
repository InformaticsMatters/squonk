# Database loaders

Squonk has a chemical database powered by the RDkit cartridge.
To use this you need to load datasets into database.
Currently there is support for
* eMolecules (bulidng blocks and screening compounds)
* ChEMBL (structures only)
* DrugBank (this is not loaded by default for licensing reasons)

The code and configuration for the loaders is in the rdkit-lib module.
At some stage it may be broken out into a separate module.

## Loading data

### Start the Postgres database

Loading requires the postgres database to be running and port 5432 to be exposed.
Normally this port is not exposed.
Move into the docker/deply dir and start the postgres database

```sh
docker-compose -f docker-compose.yml -f docker-compose-setup.yml up -d postgres
```

### Configuring loaders

The configuration is contained in a file named components/rdkit-lib/rdkit_loader.properties
You can create this by copying components/rdkit-lib/rdkit_loader.properties.template and editting accordingly.
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

### Configuring the search service

The searchsearvice needs to know what database tables have been loaded.
This is currently done using the STRUCTURE_DATABASE_TABLES environment variable
that needs to be passes to the chemservices container. To do this set this variable
in your docker/deply/setenv.sh file. The value must be a colon separated list of tabe names
with no spaces or other characters allowed. For instance:

```
export STRUCTURE_DATABASE_TABLES=emolecules_order_sc:emolecules_order_bb:chembl_23:pdb_ligand
```


