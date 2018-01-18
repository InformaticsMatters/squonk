/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.rdkit.db;

import org.squonk.rdkit.db.impl.ChemspaceTable;
import org.squonk.rdkit.db.impl.PdbLigandTable;
import org.squonk.types.MoleculeObject;
import org.squonk.rdkit.db.dsl.DataSourceConfiguration;
import org.squonk.rdkit.db.dsl.Select;
import org.squonk.rdkit.db.dsl.SqlQuery;
import org.squonk.rdkit.db.impl.ChemblTable;
import org.squonk.rdkit.db.impl.EMoleculesTable;
import org.squonk.util.IOUtils;

import javax.sql.DataSource;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by timbo on 25/04/16.
 */
public class RDKitTables {

    private static final Logger LOG = Logger.getLogger(RDKitTables.class.getName());
    private static final String SCHEMA = "vendordbs";
    private DataSourceConfiguration dbConfig;
    private Map<String, RDKitTable> rdkitTables = new LinkedHashMap<>();

    public RDKitTables(DataSource dataSource) {
        dbConfig = new DataSourceConfiguration(dataSource, Collections.emptyMap());

        String[] dbsString = IOUtils.getConfiguration("CHEMCENTRAL_DATABASE_TABLES", "").split(":");

        // This is ugly and needs improving, but it handles the current dbs that we have
        // Rather that use an environment variable we probably need to inject a configuration file
        for (String db: dbsString) {
            if (db.startsWith("emolecules_")) {
                rdkitTables.put(db, new EMoleculesTable(SCHEMA, db, MolSourceType.SMILES));
                LOG.info("Added EMoleculesTable named " + db);
            } else if (db.startsWith("chembl")) {
                rdkitTables.put(db, new ChemblTable(SCHEMA, db));
                LOG.info("Added ChemblTable named " + db);
            } else if (db.startsWith("pdb_ligand")) {
                rdkitTables.put(db, new PdbLigandTable(SCHEMA, db));
                LOG.info("Added PdbLigandTable named " + db);
            } else if (db.startsWith("chemspace")) {
                rdkitTables.put(db, new ChemspaceTable(SCHEMA, db));
                LOG.info("Added ChemspaceTable named " + db);
            } else {
                LOG.warning("Unrecognised type of table: " + db);
            }
        }

    }

    public Collection<String> getTableNames() {
        return rdkitTables.keySet();
    }

    public RDKitTable getTable(String name) {
        return rdkitTables.get(name);
    }

    public Select createSelectAll(String name) {
        RDKitTable table = rdkitTables.get(name).alias("rdk");
        return new SqlQuery(table).select();
    }

    public List<MoleculeObject> executeSelect(Select select) {
        select.setconfiguration(dbConfig);
        return select.getExecutor().execute();
    }
}
