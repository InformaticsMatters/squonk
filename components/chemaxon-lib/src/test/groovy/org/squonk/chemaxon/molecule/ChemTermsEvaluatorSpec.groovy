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

package org.squonk.chemaxon.molecule

import org.squonk.types.MoleculeObject
import org.squonk.reader.SDFReader
import spock.lang.Specification
import chemaxon.formats.MolImporter

import java.util.stream.Stream
import java.util.zip.GZIPInputStream

/**
 * Created by timbo on 14/04/2014.
 */
class ChemTermsEvaluatorSpec extends Specification {

    static Map<String,Integer> stats = new HashMap<>();


     def 'ChemTerms processor for Molecule'() {

        given:
        def atomCount = new ChemTermsEvaluator('atom_count', 'atomCount()', "")
        

        when: 
        def mol0 = MolImporter.importMol('C')
        def mol1 = MolImporter.importMol('CC')

        atomCount.processMolecule(mol0, stats)
        atomCount.processMolecule(mol1, stats)

        then:
        mol0.getPropertyObject('atom_count') == 5
        mol1.getPropertyObject('atom_count') == 8
        
    }
    
    def 'ChemTerms filter for Molecule'() {

        given:
        def atomCountLt6 = new ChemTermsEvaluator('atomCount()<6', MoleculeEvaluator.Mode.Filter, "")
        

        when:  
        def mol0 = atomCountLt6.processMolecule(MolImporter.importMol('C'), stats)
        def mol1 = atomCountLt6.processMolecule(MolImporter.importMol('CC'), stats)

        then:
        mol0 != null
        mol1 == null
        
    }


    def 'ChemTerms calc for Stream'() {

        List mols = [
                new MoleculeObject("C", "smiles"),
                new MoleculeObject("CC", "smiles"),
                new MoleculeObject("CCC", "smiles"),
                new MoleculeObject("CCCC", "smiles")
        ]

        def atomCountLt6 = new ChemTermsEvaluator('atoms', 'atomCount()<6', "")


        when:
        Stream s = mols.stream().map() {
            def result = atomCountLt6.processMoleculeObject(it, stats)
            println result
        }

        long count = s.count()

        then:
        count == 4
    }


    def 'ChemTerms calc for file'() {

        FileInputStream fis = new FileInputStream('../../data/testfiles/Kinase_inhibs.sdf.gz')
        //FileInputStream fis = new FileInputStream('../../data/testfiles/Building_blocks_GBP.sdf.gz')
        SDFReader reader = new SDFReader(new GZIPInputStream(fis))
        def atomCountLt6 = new ChemTermsEvaluator('atoms', 'atomCount()<6', "")


        when:
        Stream s = reader.asStream().map() {
            def result = atomCountLt6.processMoleculeObject(it, stats)
            //println result
        }

        long count = s.count()


        then:
        count == 36

        cleanup:
        reader?.close()
    }




}
