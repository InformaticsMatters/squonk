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

    def 'ChemTerms solubility'() {

        /* this test is because a strange error was seen for solubility

        chemservices_1  | 24-Feb-2018 09:55:22.768 WARNING [ForkJoinPool.commonPool-worker-0] org.squonk.chemaxon.molecule.ChemTermsEvaluator.evaluateMoleculeImpl Failed to evaluate chem terms expression. Property will be missing.
        chemservices_1  |  chemaxon.nfunk.jep.ParseException: Error while evaluating expression:
        chemservices_1  | logS('7.4')
        chemservices_1  |     Implementation for CalculatorValenceChecker is not found.
        chemservices_1  | 	at chemaxon.nfunk.jep.JEP.getValueAsObject(JEP.java:538)
        chemservices_1  | 	at chemaxon.jep.ChemJEP.evaluate(ChemJEP.java:152)
        chemservices_1  | 	at org.squonk.chemaxon.molecule.ChemTermsEvaluator.evaluateMoleculeImpl(ChemTermsEvaluator.java:205)
*/

        given:
        def evaluator = new ChemTermsEvaluator('logs', "logs('7.4')", "")


        when:
        def mol0 = MolImporter.importMol('C1C=CC=CC1')
        def mol1 = MolImporter.importMol('CC1C=CC=CC1')

        evaluator.processMolecule(mol0, stats)
        evaluator.processMolecule(mol1, stats)

        then:
        mol0.getPropertyObject('logs') != null
        mol1.getPropertyObject('logs') != null
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
