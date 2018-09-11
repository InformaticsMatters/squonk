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
import org.squonk.rdkit.db.tables.EMoleculesBBSmilesTable
import org.squonk.rdkit.db.tables.MolportTable
import org.squonk.util.IOUtils

/** Loader for Molport structures.
 * See https://www.molport.com/shop/database-download for details
 * Created by timbo on 16/12/2015.
 */
@Log
class MolportSmilesLoader extends AbstractRDKitLoader {

    static final String DEFAULT_FILE_DIR = "/rdkit/molport"
    static final String DEFAULT_FILE_PATTERN = /iis_smiles\-.*\.txt\.gz/

    MolportSmilesLoader(RDKitTable table, ChemcentralConfig config) {
        super(table, config)
        separator = "\t"
    }

    MolportSmilesLoader() {
        super(new MolportTable())
        separator = "\t"
    }

    @Override
    void load() {
        String dir = IOUtils.getConfiguration("CHEMCENTRAL_LOADER_DIR", DEFAULT_FILE_DIR)
        String pattern = IOUtils.getConfiguration("CHEMCENTRAL_LOADER_PATTERN", DEFAULT_FILE_PATTERN)

        def files = new FileNameByRegexFinder().getFileNames(dir, pattern)
        log.info("Found files ${files.join(',')}")
        int limit = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_REPORTING_CHUNK", "10000"))
        def propertyToTypeMappings = ['2':String.class, '5':String.class, '6':String.class, '7':String.class, '8':Integer.class]
        log.info("Using MolportSmilesLoader to load ${files.size()} files")
        loadSmilesFiles(files, limit, reportingChunk, propertyToTypeMappings)
        log.info("Loading finished")
    }

}
