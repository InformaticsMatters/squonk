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

package org.squonk.rdkit.db.loaders

import org.squonk.rdkit.db.impl.EMoleculesTable
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration

/**
 * Created by timbo on 16/12/2015.
 */
class EMoleculesSmilesLoader extends AbstractRDKitLoader {

    EMoleculesSmilesLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = loadConfigFile()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.emolecules.table
        String schema = props.database.schema
        String file = props.emolecules.path + '/' + props.emolecules.file
        int reportingChunk = props.emolecules.reportingChunk
        int loadOnly = props.emolecules.loadOnly
        Map<String, Class> propertyToTypeMappings = props.emolecules.fields

        println "Loading $file into ${schema}.$baseTable"

        EMoleculesTable table = new EMoleculesTable(schema, baseTable, MolSourceType.SMILES)

        IConfiguration config = createConfiguration(props)

        EMoleculesSmilesLoader loader = new EMoleculesSmilesLoader(table, config)
        loader.loadSmiles(file, loadOnly, reportingChunk, propertyToTypeMappings)
    }

}
