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
import org.squonk.util.IOUtils

/**
 * Created by timbo on 16/12/2015.
 */
class ChemspaceTable extends RDKitTable {

    ChemspaceTable(String schema, String baseTableName) {
        super(schema, baseTableName, MolSourceType.MOL, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("chemspace_id", "CHAR", "CHAR(12)") // //CSC000000015
        addColumn("casrn", "CHAR", "CHAR(12)") // 774544-40-0
        addColumn("mfcd", "CHAR", "CHAR(12)") // MFCD05790571
        addColumn("chemspace_url", "VARCHAR", "VARCHAR(100)") // https://chem-space.com/CSC000000015

        //CSC000000056','1266702-57-1','MFCD19244712'
    }

    ChemspaceTable() {
        this(IOUtils.getConfiguration("SCHEMA_NAME", "vendordbs"),
                IOUtils.getConfiguration("TABLE_NAME",  "chemspace"))
    }

}
