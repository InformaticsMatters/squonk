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
    final Table aliasTable;
    final Map<FingerprintType, Metric> fptypes = new LinkedHashMap<>();


    public SqlQuery(RdkTable rdkTable) {
        this.rdkTable = rdkTable;
        this.aliasTable = rdkTable.alias("t");
        this.fptypes.putAll(rdkTable.fptypes);
    }

    public Map<FingerprintType, Metric> getFingerPrintTypes() {
        return Collections.unmodifiableMap(fptypes);
    }

    List<Column> getColumns() {
        return aliasTable.columns;
    }

    public Select select(Column... cols) {
        return new Select(this, cols);
    }


}
