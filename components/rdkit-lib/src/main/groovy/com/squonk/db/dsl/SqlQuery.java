package com.squonk.db.dsl;

import com.im.lac.types.MoleculeObject;
import com.squonk.db.rdkit.FingerprintType;
import com.squonk.db.rdkit.Metric;

import java.util.*;

/**
 * Created by timbo on 13/12/2015.
 */
public class SqlQuery {

    final RdkTable rdkTable;
    final IConfiguration config;
    final Table aliasTable;


    public SqlQuery(RdkTable rdkTable, IConfiguration config) {
        this.rdkTable = rdkTable;
        this.config = config;
        this.aliasTable = rdkTable.alias("t");
    }

    public List<FingerprintType> getFingerPrintTypes() {

        return Collections.unmodifiableList(rdkTable.fptypes);
    }

    List<Column> getColumns() {
        return aliasTable.columns;
    }

    public Select select(Column... cols) {
        return new Select(this, cols);
    }


}
