package com.im.lac.chemaxon.molecule

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule

/**
 * Created by timbo on 14/04/2014.
 */
class ChemTermsEvaluatorSpec extends Specification {




    def 'ChemTerms processor for List'() {

        given:
        def atomCount = new ChemTermsEvaluator('atomCount', 'atom_count')

        when:
        def mols = []
        mols << MolImporter.importMol('C')
        mols << MolImporter.importMol('CC')        
        mols << MolImporter.importMol('CCC')
        atomCount.evaluateMolecules(mols)

        then:
        mols[0].getPropertyObject('atom_count') == 5
        mols[1].getPropertyObject('atom_count') == 8
        mols[2].getPropertyObject('atom_count') == 11
        
    }
    
     def 'ChemTerms processor for Molecule'() {

        given:
        def atomCount = new ChemTermsEvaluator('atomCount', 'atom_count')
        

        when: 
        def mol0 = MolImporter.importMol('C')
        def mol1 = MolImporter.importMol('CC')
        atomCount.evaluateMolecule(mol0)
        atomCount.evaluateMolecule(mol1)
        

        then:
        mol0.getPropertyObject('atom_count') == 5
        mol1.getPropertyObject('atom_count') == 8
        
    }

  
}
