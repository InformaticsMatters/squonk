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

import org.squonk.rdkit.db.dsl.Select;
import org.squonk.rdkit.db.dsl.SqlQuery;
import org.squonk.types.MoleculeObject;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by timbo on 25/04/16.
 */
public class RDKitTables {

    private static final Logger LOG = Logger.getLogger(RDKitTables.class.getName());
    private final ChemcentralConfig config;

    public RDKitTables(ChemcentralConfig config) {
        this.config = config;
    }

    public Collection<String> getTableNames() {
        return config.getRDKitTables().keySet();
    }

    public RDKitTable getTable(String name) {
        return config.getRDKitTable(name);
    }

    public Select createSelectAll(String name) {
        RDKitTable table = getTable(name).alias("rdk");
        return new SqlQuery(table).select();
    }

    public List<MoleculeObject> executeSelect(Select select) {
        select.setconfiguration(config);
        return select.getExecutor().execute();
    }
}
