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
class LTKBTable extends RDKitTable {

    LTKBTable(String schema, String baseTableName) {
        super(schema, baseTableName, MolSourceType.SMILES, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("ltkbid", "CHAR", "CHAR(7) NOT NULL")
        addColumn("drug_name", "TEXT", "TEXT NOT NULL")
        addColumn("approval_year", "INTEGER", "INTEGER")
        addColumn("dili_concern", "INTEGER", "INTEGER")
        addColumn("vdili_concern", "INTEGER", "INTEGER")
        addColumn("severity_class", "INTEGER", "INTEGER")
        addColumn("label_section", "TEXT", "TEXT")
        addColumn("greene_annotation", "TEXT", "TEXT")
        addColumn("sakatis_annotation", "TEXT", "TEXT")
        addColumn("xu_annotation", "TEXT", "TEXT")
        addColumn("zhu_annotation", "TEXT", "TEXT")
    }

    LTKBTable() {
        this(IOUtils.getConfiguration("CHEMCENTRAL_SCHEMA", "vendordbs"),
                IOUtils.getConfiguration("CHEMCENTRAL_TABLE", "ltkb"))
    }
}
