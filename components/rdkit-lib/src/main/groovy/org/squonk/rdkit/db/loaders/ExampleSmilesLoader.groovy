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

import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration
import org.squonk.rdkit.db.impl.ExampleTable
import org.squonk.util.IOUtils

/** Example smiles loader that is used for testing.
 * This is modelled on the eMolecules tables and data.
 *
 * Created by timbo on 16/12/2015.
 */
class ExampleSmilesLoader extends AbstractRDKitLoader {

    ExampleSmilesLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }

    ExampleSmilesLoader(IConfiguration config) {
        this(new ExampleTable(
                IOUtils.getConfiguration("SCHEMA_NAME", "vendordbs"),
                IOUtils.getConfiguration("TABLE_NAME", "emols_test"),
                MolSourceType.SMILES), config
        )
    }

    ExampleSmilesLoader() {
        super(new ExampleTable(
                IOUtils.getConfiguration("SCHEMA_NAME", "vendordbs"),
                IOUtils.getConfiguration("TABLE_NAME", "emols_test"),
                MolSourceType.SMILES)
        )
    }

    @Override
    void load() {
        String filename = IOUtils.getConfiguration("LOADER_FILE", "../../data/testfiles/emols_100.smi.gz")
        int limit = new Integer(IOUtils.getConfiguration("LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("REPORTING_CHUNK", "10"))
        def propertyToTypeMappings = ['1': Integer.class, '2': Integer.class]
        loadSmiles(filename, limit, reportingChunk, propertyToTypeMappings)
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

        ExampleTable table = new ExampleTable(schema, baseTable, MolSourceType.SMILES)

        IConfiguration config = createConfiguration(props)

        ExampleSmilesLoader loader = new ExampleSmilesLoader(table, config)
        loader.loadSmiles(file, loadOnly, reportingChunk, propertyToTypeMappings)
    }

}
