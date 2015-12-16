package com.squonk.db.dsl;

import com.squonk.db.rdkit.FingerprintType;
import com.squonk.db.rdkit.Metric;
import com.squonk.db.rdkit.MolSourceType;

import java.util.*;

/**
 * Created by timbo on 14/12/2015.
 */
public class RdkTable extends Table {

    final List<FingerprintType> fptypes = new ArrayList<>();
    final MolSourceType molSourceType;
    final Table molfpsTable;

    public RdkTable(String baseTableName, MolSourceType molSourceType, Collection<FingerprintType> fptypes) {
        this(null, baseTableName, molSourceType, fptypes);
    }

    public RdkTable(String schema, String name, MolSourceType molSourceType, Collection<FingerprintType> fptypes) {
        super(schema, name);
        addColumn("id", "SERIAL", "SERIAL PRIMARY KEY");
        addColumn("structure", "TEXT", "TEXT");
        this.molSourceType = molSourceType;
        this.fptypes.addAll(fptypes);
        this.molfpsTable = new Table(getBaseTable() + "_molfps")
                .column("id", "INTEGER", "INTEGER NOT NULL PRIMARY KEY")
                .column("m", "MOL", "MOL");
    }

    public List<FingerprintType> getFingerprintTypes() {
        return Collections.unmodifiableList(fptypes);
    }

    public MolSourceType getMolSourceType() {
        return molSourceType;
    }

    public Table getMolFpTable() {
        return molfpsTable;
    }
}
