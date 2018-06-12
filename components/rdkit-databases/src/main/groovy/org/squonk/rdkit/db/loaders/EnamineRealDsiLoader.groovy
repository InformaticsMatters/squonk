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
import org.squonk.rdkit.db.ChemcentralConfig
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.tables.EnamineRealDsiTable
import org.squonk.util.IOUtils

/** Loader for the Enamine REAL analogues based around the XChem screening library.
 * Download from http://enamine.net/files/REAL/2018q1-2/Jun2018_REAL_analogs_DSI_library_39M_MW_HA_smiles.zip
 *
 * Created by timbo on 12/06/2018.
 */
@Log
class EnamineRealDsiLoader extends AbstractRDKitLoader {

    static final String DEFAULT_FILE_NAME = "REAL_analogs_DSI_library_smiles.gz"

    EnamineRealDsiLoader(RDKitTable table, ChemcentralConfig config) {
        super(table, config)
    }

    EnamineRealDsiLoader() {
        super(new EnamineRealDsiTable())
    }

    @Override
    void load() {
        String filename = IOUtils.getConfiguration("CHEMCENTRAL_LOADER_FILE", DEFAULT_FILE_NAME)
        int limit = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_REPORTING_CHUNK", "100000"))
        def propertyToTypeMappings = ['1':String.class]
        log.info("Using EnamineRealDsiLoader to load $filename")
        loadSmiles(filename, limit, reportingChunk, propertyToTypeMappings)
        log.info("Loading finished")
    }

}
