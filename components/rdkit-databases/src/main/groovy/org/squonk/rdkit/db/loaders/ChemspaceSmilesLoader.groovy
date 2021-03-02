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

package org.squonk.rdkit.db.loaders

import groovy.util.logging.Log
import org.squonk.rdkit.db.ChemcentralConfig
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.tables.ChemspaceTable
import org.squonk.util.IOUtils

/** Loader for chemspace building blocks dataset.
 * This data was obtained directly from ChemSpace
 * Created by timbo on 02/03/2021.
 */
@Log
class ChemspaceSmilesLoader extends AbstractRDKitLoader {

    static final String DEFAULT_FILE_NAME = "/rdkit/chemspace.smiles.gz";

    ChemspaceSmilesLoader(RDKitTable table, ChemcentralConfig config) {
        super(table, config)
    }

    ChemspaceSmilesLoader() {
        super(new ChemspaceTable())
    }

    @Override
    void load() {
        String filename = IOUtils.getConfiguration("CHEMCENTRAL_LOADER_FILE", DEFAULT_FILE_NAME)
        int limit = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_REPORTING_CHUNK", "10000"))
        def propertyToTypeMappings = ['1':String.class, '2':Integer.class]
        log.info("Using ChemspaceSmilesLoader to load $filename")
        loadSmiles(filename, limit, reportingChunk, propertyToTypeMappings)
        log.info("Loading finished")
    }

}
