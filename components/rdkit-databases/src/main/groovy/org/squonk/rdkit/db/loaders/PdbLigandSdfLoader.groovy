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
import org.RDKit.ROMol
import org.squonk.rdkit.db.ChemcentralConfig
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.impl.PdbLigandTable
import org.squonk.rdkit.mol.MolReader
import org.squonk.types.MoleculeObject
import org.squonk.util.IOUtils

import java.util.stream.Stream

/** Loader for PDB ligand structures.
 * Find file to download from here http://ligand-expo.rcsb.org/ld-download.html and put in:
 * the file you want is in the "Chemical component coordinate data files" section and called all-sdf.sdf.gz
 * fetch it with something like:
 * wget http://ligand-expo.rcsb.org/dictionaries/all-sdf.sdf.gz
 *
 * Created by timbo on 16/12/2015.
 */
@Log
class PdbLigandSdfLoader extends AbstractRDKitLoader {

    static final String DEFAULT_TABLE_NAME = "all-sdf.sdf.gz";

    PdbLigandSdfLoader(RDKitTable table, ChemcentralConfig config) {
        super(table, config)
    }

    PdbLigandSdfLoader() {
        super(new PdbLigandTable())
    }

    @Override
    void load() {
        String filename = IOUtils.getConfiguration("CHEMCENTRAL_LOADER_FILE", DEFAULT_TABLE_NAME)
        int limit = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_LIMIT", "0"))
        int reportingChunk = new Integer(IOUtils.getConfiguration("CHEMCENTRAL_REPORTING_CHUNK", "10000"))
        def propertyToTypeMappings = [pdb_code:String.class, ligand_code:String.class]
        log.info("Using PdbLigandSdfLoader to load $filename")
        loadSDF(filename, limit, reportingChunk, propertyToTypeMappings, "ligand_code")
        log.info("Loading finished")
    }

    protected Stream<MoleculeObject> prepareStream(Stream<MoleculeObject> stream) {
        return stream.filter() { mo ->
            try {
                ROMol mol = MolReader.generateMolFromMolfile(mo.source)
                long heavyAtomCount = mol.getNumHeavyAtoms()
                if (heavyAtomCount <= 5) {
                    println "${mo.getValue("ligand_code")} too small"
                }
                return heavyAtomCount > 5
            } catch (Throwable e) {
                log.info("failed to parse molecule " + mo.getValue("ligand_code"))
                return false
            }
        }.peek() { mo ->
            String code = mo.getValue("ligand_code")
            String pdbCode = code.substring(0, 4).toUpperCase()
            //println "$pdbCode -> $code"
            mo.putValue("pdb_code", pdbCode)
        }
    }

}
