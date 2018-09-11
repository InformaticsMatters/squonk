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

import org.squonk.rdkit.db.ChemcentralConfig
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.RDKitTableLoader
import org.squonk.rdkit.db.dsl.SqlQuery
import org.squonk.reader.SDFReader
import org.squonk.types.MoleculeObject
import org.squonk.util.IOUtils

import java.util.stream.Stream

/**
 * Created by timbo on 16/12/2015.
 */
abstract class AbstractRDKitLoader {

    final RDKitTable table
    final ChemcentralConfig config

    protected String separator = "\\s+"


    AbstractRDKitLoader(RDKitTable table, ChemcentralConfig config) {
        this.table = table
        this.config = config
    }

    AbstractRDKitLoader(RDKitTable table) {
        this.table = table
        this.config = new ChemcentralConfig()
    }

    protected Stream<MoleculeObject> prepareStream(Stream<MoleculeObject> stream) {
        return stream
    }

    abstract void load()


    protected void loadSDF(String file, int limit, int reportingChunk, Map<String, Class> propertyToTypeMappings, String nameFieldName) {
        SqlQuery q = new SqlQuery(table, config)

        println "Loading file $file"
        long t0 = System.currentTimeMillis()
        InputStream is = IOUtils.getGunzippedInputStream(new FileInputStream(file))
        try {
            SDFReader sdf = new SDFReader(is)
            sdf.setNameFieldName(nameFieldName)
            Stream<MoleculeObject> mols = sdf.asStream()

            if (limit > 0) {
                mols = mols.limit(limit)
            }

            mols = prepareStream(mols)

            RDKitTableLoader loader = q.loader()
            loader.reportingSize = reportingChunk
            doLoad(loader, mols, propertyToTypeMappings)

        } finally {
            is.close()
        }
        long t1 = System.currentTimeMillis()
        println "Completed in ${t1 - t0}ms"
    }

    protected void loadSmiles(String file, int limit, int reportingChunk, Map<String, Class> propertyToTypeMappings) {
        loadSmilesFiles(Collections.singletonList(file), limit, reportingChunk, propertyToTypeMappings)
    }

    protected void loadSmilesFiles(List<String> files, int limit, int reportingChunk, Map<String, Class> propertyToTypeMappings) {
        SqlQuery q = new SqlQuery(table, config)

        println "setting reportingSize to $reportingChunk"
        RDKitTableLoader loader = q.loader()
        loader.reportingSize = reportingChunk

        // create the tables
        prepareTables(loader)

        files.each { file ->

            println "Loading file $file"
            long t0 = System.currentTimeMillis()
            InputStream is = IOUtils.getGunzippedInputStream(new FileInputStream(file))
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try {

                Stream<MoleculeObject> mols = reader.lines().skip(1).map() { String line ->
                    //println line
                    String[] parts = line.split(separator)
                    def values = [:]
                    parts[1..-1].eachWithIndex { String entry, int i ->
                        values["" + (i + 1)] = entry
                    }
                    MoleculeObject mo = new MoleculeObject(parts[0], 'smiles', values);
                    return mo;
                }

                if (limit > 0) {
                    mols = mols.limit(limit)
                }

                loader.loadData(mols, propertyToTypeMappings)

            } finally {
                reader.close()
            }
            long t1 = System.currentTimeMillis()
            println "Loaded in ${t1 - t0}ms"
        }

        processTables(loader)

    }


    protected void doLoad(RDKitTableLoader worker, Stream<MoleculeObject> mols, Map<String, Class> propertyToTypeMappings) {

        prepareTables(worker)
        worker.loadData(mols, propertyToTypeMappings)
        processTables(worker)
    }

    protected prepareTables(RDKitTableLoader worker) {
        worker.dropAllItems()
        worker.removeTableInfo()
        worker.createTables()
    }

    protected processTables(RDKitTableLoader worker) {
        worker.createMoleculesAndIndex()
        worker.addFpColumns()
        worker.putTableInfo()
        worker.aliasTableInfo()

        worker.getRowCount()
        worker.analyze()

        //worker.dropAllItems()
    }



}
