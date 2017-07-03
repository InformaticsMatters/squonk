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

import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration
import org.squonk.rdkit.db.impl.ChemblTable

/**
 * Created by timbo on 16/12/2015.
 */
class ChemblSdfLoader extends AbstractRDKitLoader {

    ChemblSdfLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = loadConfigFile()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.chembl.table
        String schema = props.database.schema
        String file = props.chembl.path + '/' + props.chembl.file
        int reportingChunk = props.chembl.reportingChunk
        int loadOnly = props.chembl.loadOnly
        Map<String, Class> propertyToTypeMappings = props.chembl.fields

        println "Loading $file into ${schema}.$baseTable"

        ChemblTable table = new ChemblTable(schema, baseTable)

        IConfiguration config = createConfiguration(props)

        ChemblSdfLoader loader = new ChemblSdfLoader(table, config)
        loader.loadSDF(file, loadOnly, reportingChunk, propertyToTypeMappings, null)
    }

}
