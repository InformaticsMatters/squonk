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

import chemaxon.util.ConnectionHandler

import chemaxon.jchem.db.UpdateHandler
import chemaxon.standardizer.Standardizer
import chemaxon.struc.Molecule
import chemaxon.formats.MolImporter
import chemaxon.calculations.clean.Cleaner
import groovy.sql.Sql
import org.squonk.chemaxon.molecule.MoleculeUtils

/**
 *
 * @author timbo
 */
class StructureLoader {
    
    private ConnectionHandler conh
    private UpdateHandler uh
    private String structureTable
    private String propertyTable
    private Sql db
    private Standardizer cleaner
    private String updateParentIdSql
	
    StructureLoader(Sql db, String structureTable, String propertyTable) {
        this.structureTable = structureTable
        this.propertyTable = propertyTable
        this.db = db
        init()
    }
 
    private void init() {
    
        conh = new ConnectionHandler(db.connection, propertyTable);

        uh = new UpdateHandler(conh, UpdateHandler.INSERT, structureTable, null);
        uh.duplicateFiltering = UpdateHandler.DUPLICATE_FILTERING_ON
        uh.ignoreChemicalTermsExceptions = true
        uh.loggingEnabled = false
        cleaner = new Standardizer(new File('pre-insert-standardizer.xml'))
        updateParentIdSql = "UPDATE $structureTable SET parent_id = ? WHERE cd_id = ?"
    }
    
    void close() {
        uh?.close()
    }
    
    int execute(def mol) {
        Molecule m = clean(mol)
        int id = insertStructure(m)
        if (id > 0) {
            int parentId = insertParent(m, id)
            updateParentIdForChild(id, Math.abs(parentId))
        }
        return id
    }
    
    private int insertStructure(Molecule mol) {
        String format = mol.inputFormat
        uh.structure = MoleculeUtils.exportAsString(mol, format, 'mol', 'mrv') 
        int id = uh.execute(true)
        return id
    }
    
    Molecule clean(def mol) {
        Molecule m
        if (mol instanceof Molecule) {
            m = mol
        } else {
            m = MolImporter.importMol(mol)
        }
        if (m.getDim() != 2) {
            Cleaner.clean(m, 2, "t1000")
        }
        cleaner.standardize(m)
        return m
    }
    
    int insertParent(Molecule mol, int childId) {
        Molecule parentMol = LoaderUtils.findParentStructure(mol)
        //println "Child is $mol parent is $parentMol"
        int parentId = 0
        if (parentMol.is(mol)) { // no frags
            parentId = childId
        } else {
            parentId = insertStructure(parentMol)
        }
        return parentId
    }
     
    void updateParentIdForChild(int childId, int parentId) {
        db.executeUpdate(updateParentIdSql, [parentId, childId])
    }

}