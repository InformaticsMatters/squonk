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

package org.squonk.camel.chemaxon.processor.db

import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.jchem.db.UpdateHandler
import chemaxon.struc.Molecule
import chemaxon.jchem.db.TableTypeConstants
import org.squonk.chemaxon.db.UpdateHandlerSupport
import java.sql.Connection

/**
 *
 * @author timbo
 */
class DBUtils {
    
    static void createStructureTableWithData(Connection con, InputStream is, String format,
        String tableName, int tableType, String standardizer, 
        Map<String,String> extraCols, Map<String, Class> fieldTypes, Map<String,String> fieldToColNameMapping) {
        
        String cols = extraCols.collect() {
            it.key + " " + it.value
        }.join(',')
        
        UpdateHandlerSupport uhs = new UpdateHandlerSupport()
        uhs.connection = con
        uhs.start()
        def worker
        MolImporter importer
        try {
            uhs.createPropertyTable(false)
            uhs.createStructureTable(tableName, tableType, standardizer, cols)
        
            importer = new MolImporter(is)
            Iterator<Molecule> iter = importer.iterator()

            worker = uhs.createWorker(UpdateHandler.INSERT, tableName, extraCols.keySet().join(","))
            worker.updateHandler.emptyStructuresAllowed = true
            while (iter.hasNext()) {
                Molecule mol = iter.next()
                //println "Mol is $mol and has ${mol.getAtomCount()} atoms"
                String molStr = MolExporter.exportToFormat(mol, format)
                //println "MolStr is $molStr"
                Map props = [:]
                fieldTypes.each { k,v ->
                    def o = mol.getPropertyObject(k)
                    //println "converting $o to type $v"
                    o = convertToType(o, v)
                    props[k] = o
                }
                List vals = []
                extraCols.keySet().each {
                    String fldName = fieldToColNameMapping[it] ?: it
                    vals << props[fldName]
                }
                worker.execute(molStr, vals.toArray())
            }
        } finally {
            worker?.close()
            uhs?.stop()
            importer?.close()
        }
        
    }
            
    
    static Object convertToType(String val, Class cls) {
        if (cls == String.class) {
            return val
        }
        return cls.newInstance([val] as Object[])
    }
    
    static void createNci1000StructureTable(Connection con, String tableName) { 
        createStructureTableWithData(con, new FileInputStream("../../data/testfiles/nci1000.smiles"), "smiles",
            tableName, TableTypeConstants.TABLE_TYPE_MOLECULES, null, [:], [:], [:])
    }
    static void createDHFRStructureTable(Connection con, String tableName) { 
        createStructureTableWithData(con, new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"), "mol",
            tableName, TableTypeConstants.TABLE_TYPE_MOLECULES, null, 
            [name:'varchar(1000)', family:'varchar(10)', pc_um:'varchar(16)', tg_um:'varchar(16)', rl_um:'varchar(16)', mset:'integer'], 
            [Name:String.class, Family:String.class, PC_uM:String.class, TG_uM:String.class, RL_uM:String.class, 'set':Integer.class], 
            [name:'Name', family:'Family', pc_um:'PC_uM', tg_um:'TG_uM', rl_um:'TG_uM', mset:'set'])
        // these Maps are ugly. Find something better
    }
    
    
}
	


