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
import org.squonk.rdkit.db.tables.ChemspaceTable
import org.squonk.util.IOUtils

/** Loader for ChEMBL structures.
 * Download the SDF from ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/
 *
 *
 * Created by timbo on 16/12/2015.
 */
@Log
class ChemspaceSdfLoader extends AbstractRDKitLoader {

    static final String DEFAULT_TABLE_NAME = "chemspace.sdf.gz";

    ChemspaceSdfLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }

    ChemspaceSdfLoader() {
        super(new ChemspaceTable())
    }

    @Override
    void load() {
        String filename = IOUtils.getConfiguration("LOADER_FILE", DEFAULT_TABLE_NAME)
        int limit = new Integer(IOUtils.getConfiguration("LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("REPORTING_CHUNK", "10000"))
        def propertyToTypeMappings = [CHEMSPACE_ID: String.class, CAS: String.class, MFCD: String.class, CHEMSPACE_URL: String.class]
        log.info("Using ChemspaceSdfLoader to load $filename")
        loadSDF(filename, limit, reportingChunk, propertyToTypeMappings, null)
        log.info("Loading finished")
    }

}
