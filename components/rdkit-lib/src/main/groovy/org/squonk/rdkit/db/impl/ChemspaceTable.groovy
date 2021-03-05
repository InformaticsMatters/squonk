/*
 * Copyright (c) 2021 Informatics Matters Ltd.
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

package org.squonk.rdkit.db.impl

import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import org.squonk.util.IOUtils

/**
 * Created by timbo on 16/12/2015.
 */
class ChemspaceTable extends RDKitTable {

    ChemspaceTable(String schema, String baseTableName) {
        super(schema, baseTableName, MolSourceType.SMILES, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("chemspace_id", "CHAR", "CHAR(15)") // // CSMB02516611889
        addColumn("price_1g_usd", "INTEGER", "INTEGER") // 1357
    }

    ChemspaceTable() {
        this(IOUtils.getConfiguration("CHEMCENTRAL_SCHEMA", "vendordbs"),
                IOUtils.getConfiguration("CHEMCENTRAL_TABLE",  "chemspace"))
    }

}
