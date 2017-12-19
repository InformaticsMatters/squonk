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

package org.squonk.rdkit.db.tables

import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable

/**
 * Created by timbo on 16/12/2015.
 */
class DrugbankTable extends RDKitTable {

    DrugbankTable(String schema, String baseTableName) {
        super(schema, baseTableName, MolSourceType.MOL, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("drugbank_id", "CHAR", "CHAR(7)")
        addColumn("drug_groups", "TEXT", "TEXT")
        addColumn("generic_name", "TEXT", "TEXT")
        addColumn("brands", "TEXT", "TEXT")
        addColumn("products", "TEXT", "TEXT")
    }

}
