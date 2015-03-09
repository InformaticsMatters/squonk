package com.im.lac.chemaxon.molecule

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule

/**
 * Created by timbo on 14/04/2014.
 */
class ChemTermsEvaluatorSpec extends Specification {


     def 'ChemTerms processor for Molecule'() {

        given:
        def atomCount = new ChemTermsEvaluator('atom_count', 'atomCount()')
        

        when: 
        def mol0 = MolImporter.importMol('C')
        def mol1 = MolImporter.importMol('CC')
        atomCount.processMolecule(mol0)
        atomCount.processMolecule(mol1)

        then:
        mol0.getPropertyObject('atom_count') == 5
        mol1.getPropertyObject('atom_count') == 8
        
    }
    
    def 'ChemTerms filter for Molecule'() {

        given:
        def atomCountLt6 = new ChemTermsEvaluator('atomCount()<6', MoleculeEvaluator.Mode.Filter)
        

        when:  
        def mol0 = atomCountLt6.processMolecule(MolImporter.importMol('C'))
        def mol1 = atomCountLt6.processMolecule(MolImporter.importMol('CC'))

        then:
        mol0 != null
        mol1 == null
        
    }

  
}
