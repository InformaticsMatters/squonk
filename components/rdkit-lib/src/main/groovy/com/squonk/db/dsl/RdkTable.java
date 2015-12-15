package com.squonk.db.dsl;

import com.squonk.db.rdkit.FingerprintType;
import com.squonk.db.rdkit.Metric;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by timbo on 14/12/2015.
 */
public class RdkTable extends Table {

    final Map<FingerprintType, Metric> fptypes = new LinkedHashMap<>();

    public RdkTable(String baseTableName, Map<FingerprintType, Metric> fptypes) {
        super(baseTableName);
        this.fptypes.putAll(fptypes);
    }

    public RdkTable(String schema, String name, Map<FingerprintType, Metric> fptypes) {
        super(schema, name);
        this.fptypes.putAll(fptypes);
    }

    public Map<FingerprintType, Metric> getFingerprintTypes() {
        return Collections.unmodifiableMap(fptypes);
    }
}
