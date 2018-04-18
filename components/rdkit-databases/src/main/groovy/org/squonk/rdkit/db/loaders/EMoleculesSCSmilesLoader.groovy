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

import groovy.util.logging.Log
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration
import org.squonk.rdkit.db.tables.EMoleculesSCTable
import org.squonk.util.IOUtils

/** Loader for eMolecules screening compounds dataset.
 * Download eMolecules in smiles format from http://emolecules.com/info/plus/download-database
 * Created by timbo on 16/12/2015.
 */
@Log
class EMoleculesSCSmilesLoader extends AbstractRDKitLoader {

    static final String DEFAULT_TABLE_NAME = "version.smi.gz";

    EMoleculesSCSmilesLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }

    EMoleculesSCSmilesLoader() {
        super(new EMoleculesSCTable())
    }

    @Override
    void load() {
        String filename = IOUtils.getConfiguration("LOADER_FILE", DEFAULT_TABLE_NAME)
        int limit = new Integer(IOUtils.getConfiguration("LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("REPORTING_CHUNK", "10000"))
        def propertyToTypeMappings = ['1':Integer.class, '2':Integer.class]
        log.info("Using EMoleculesSCSmilesLoader to load $filename")
        loadSmiles(filename, limit, reportingChunk, propertyToTypeMappings)
        log.info("Loading finished")
    }

}
