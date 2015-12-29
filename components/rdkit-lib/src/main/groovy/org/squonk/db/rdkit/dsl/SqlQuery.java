package org.squonk.db.rdkit.dsl;

import org.squonk.db.rdkit.FingerprintType;
import org.squonk.db.rdkit.RDKitTable;
import org.squonk.db.rdkit.RDKitTableLoader;

import java.util.*;

/**
 * Created by timbo on 13/12/2015.
 */
public class SqlQuery {

    final RDKitTable rdkTable;
    final IConfiguration config;
    final Table aliasTable;


    public SqlQuery(RDKitTable rdkTable, IConfiguration config) {
        this.rdkTable = rdkTable;
        this.config = config;
        this.aliasTable = rdkTable.alias("t");
    }

    public List<FingerprintType> getFingerPrintTypes() {
        return Collections.unmodifiableList(rdkTable.getFingerprintTypes());
    }

    List<Column> getColumns() {
        return aliasTable.columns;
    }

    public Select select(Column... cols) {
        return new Select(this, cols);
    }

    public RDKitTableLoader loader() {
        return new RDKitTableLoader(rdkTable, config);
    }


}
