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

    public RdkTable(String baseTableName, MolSourceType molSourceType, Collection<FingerprintType> fptypes) {
        super(baseTableName);
        this.molSourceType = molSourceType;
        this.fptypes.addAll(fptypes);
    }

    public RdkTable(String schema, String name, MolSourceType molSourceType, Collection<FingerprintType> fptypes) {
        super(schema, name);
        this.molSourceType = molSourceType;
        this.fptypes.addAll(fptypes);
    }

    public List<FingerprintType> getFingerprintTypes() {
        return Collections.unmodifiableList(fptypes);
    }

    public MolSourceType getMolSourceType() {
        return molSourceType;
    }
}
