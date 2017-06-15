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

import org.squonk.types.MoleculeObject
import org.RDKit.ROMol
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration
import org.squonk.rdkit.db.impl.PdbLigandTable
import org.squonk.rdkit.mol.MolReader

import java.util.stream.Stream

/**
 * Created by timbo on 16/12/2015.
 */
class PdbLigandSDFLoader extends AbstractRDKitLoader {

    PdbLigandSDFLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = loadConfigFile()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.pdbligand.table
        String schema = props.database.schema
        String file = props.pdbligand.path + '/' + props.pdbligand.file
        int reportingChunk = props.pdbligand.reportingChunk
        int loadOnly = props.pdbligand.loadOnly
        Map<String, Class> propertyToTypeMappings = props.pdbligand.fields

        println "Loading $file into ${schema}.$baseTable"

        PdbLigandTable table = new PdbLigandTable(schema, baseTable)

        IConfiguration config = createConfiguration(props)

        PdbLigandSDFLoader loader = new PdbLigandSDFLoader(table, config)
        loader.loadSDF(file, loadOnly, reportingChunk, propertyToTypeMappings, "ligand_code")
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
                println "failed to parse molecule " + mo.getValue("ligand_code")
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
