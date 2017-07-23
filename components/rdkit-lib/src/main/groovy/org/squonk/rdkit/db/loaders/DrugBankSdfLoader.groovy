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

import org.squonk.rdkit.db.impl.DrugbankTable
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration

/**
 * Created by timbo on 16/12/2015.
 */
class DrugBankSdfLoader extends AbstractRDKitLoader {

    DrugBankSdfLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = loadConfigFile()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.drugbank.table
        String schema = props.database.schema
        String file = props.drugbank.path + '/' + props.drugbank.file
        int reportingChunk = props.drugbank.reportingChunk
        int loadOnly = props.drugbank.loadOnly
        Map<String, Class> propertyToTypeMappings = props.drugbank.fields

        println "Loading $file into ${schema}.$baseTable"

        DrugbankTable table = new DrugbankTable(schema, baseTable)

        IConfiguration config = createConfiguration(props)

        DrugBankSdfLoader loader = new DrugBankSdfLoader(table, config)
        loader.loadSDF(file, loadOnly, reportingChunk, propertyToTypeMappings)
    }

}
