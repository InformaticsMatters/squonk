package org.squonk.rdkit.db;

import com.im.lac.types.MoleculeObject;
import org.squonk.rdkit.db.dsl.DataSourceConfiguration;
import org.squonk.rdkit.db.dsl.Select;
import org.squonk.rdkit.db.dsl.SqlQuery;
import org.squonk.rdkit.db.impl.EMoleculesTable;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by timbo on 25/04/16.
 */
public class RDKitTables {

    private static final String SCHEMA = "vendordbs";
    private DataSourceConfiguration dbConfig;
    private Map<String, RDKitTable> rdkitTables = new LinkedHashMap<>();

    public RDKitTables(DataSource dataSource) {
        dbConfig = new DataSourceConfiguration(dataSource, Collections.emptyMap());
        rdkitTables.put("emolecules_order_bb", new EMoleculesTable(SCHEMA, "emolecules_order_bb", MolSourceType.SMILES));
        rdkitTables.put("emolecules_order_all", new EMoleculesTable(SCHEMA, "emolecules_order_all", MolSourceType.SMILES));
        rdkitTables.put("chembl_21", new EMoleculesTable(SCHEMA, "chembl_21", MolSourceType.MOL));
    }

    public Collection<String> getTableNames() {
        return rdkitTables.keySet();
    }

    public RDKitTable getTable(String name) {
        return rdkitTables.get(name);
    }

    public Select createSelectAll(String name) {
        RDKitTable table = rdkitTables.get(name).alias("rdk");
        return new SqlQuery(table).select(table.getColumns().subList(1, table.getColumns().size()));
    }

    public List<MoleculeObject> executeSelect(Select select) {
        select.setconfiguration(dbConfig);
        return select.getExecutor().execute();
    }
}
