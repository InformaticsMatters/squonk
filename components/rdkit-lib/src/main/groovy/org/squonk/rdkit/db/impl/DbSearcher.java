package org.squonk.rdkit.db.impl;

import com.im.lac.types.MoleculeObject;
import org.squonk.rdkit.db.MolSourceType;
import org.squonk.rdkit.db.RDKitTable;
import org.squonk.rdkit.db.dsl.DataSourceConfiguration;
import org.squonk.rdkit.db.dsl.Select;
import org.squonk.rdkit.db.dsl.SqlQuery;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by timbo on 25/04/16.
 */
public class DbSearcher {

    private static final String SCHEMA = "vendordbs";
    private DataSourceConfiguration dbConfig;
    private Map<String, RDKitTable> rdkitTables = new LinkedHashMap<>();

    public DbSearcher(DataSource dataSource) {
        dbConfig = new DataSourceConfiguration(dataSource, Collections.emptyMap());
        rdkitTables.put("emolecules_order_bb", new EMoleculesTable(SCHEMA, "emolecules_order_bb", MolSourceType.SMILES));
        rdkitTables.put("emolecules_order_all", new EMoleculesTable(SCHEMA, "emolecules_order_all", MolSourceType.SMILES));
    }

    public Collection<String> getTablesNames() {
        return rdkitTables.keySet();
    }

    public RDKitTable getTable(String name) {
        return rdkitTables.get(name);
    }

    public Select createSelect(String name) {
        RDKitTable table = rdkitTables.get(name);
        return new SqlQuery(table, null).select();
    }

    public List<MoleculeObject> executeSelect(Select select) {
        select.setconfiguration(dbConfig);
        return select.getExecutor().execute();
    }
}
