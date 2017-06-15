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

import org.squonk.rdkit.db.dsl.Table;

import java.util.*;

/**
 * Created by timbo on 14/12/2015.
 */
public class RDKitTable extends Table {

    final List<FingerprintType> fptypes = new ArrayList<>();
    final MolSourceType molSourceType;
    final Table molfpsTable;

    public RDKitTable(String baseTableName, MolSourceType molSourceType, Collection<FingerprintType> fptypes) {
        this(null, baseTableName, molSourceType, fptypes);
    }

    public RDKitTable(String schema, String name, MolSourceType molSourceType, Collection<FingerprintType> fptypes) {
        this(schema, name, molSourceType, fptypes, "mfp");
    }

    public RDKitTable(String schema, String name, MolSourceType molSourceType, Collection<FingerprintType> fptypes, String molfpsAlias) {
        super(schema, name);
        addColumn("id", "SERIAL", "SERIAL PRIMARY KEY");
        addColumn("structure", "TEXT", "TEXT");
        this.molSourceType = molSourceType;
        this.fptypes.addAll(fptypes);
        Table mfp = new Table(schema, getBaseName() + "_molfps")
                .column("id", "INTEGER", "INTEGER NOT NULL PRIMARY KEY")
                .column("m", "MOL", "MOL");
        this.molfpsTable = molfpsAlias == null ? mfp : mfp.alias(molfpsAlias);
    }

    private RDKitTable(String alias, RDKitTable table) {
        super(alias, table);
        this.molSourceType = table.getMolSourceType();
        this.molfpsTable = table.getMolFpTable();
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

    public RDKitTable alias(String alias) {
        return new RDKitTable(alias, this);
    }
}
