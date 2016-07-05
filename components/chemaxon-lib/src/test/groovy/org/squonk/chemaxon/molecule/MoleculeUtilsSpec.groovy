package org.squonk.chemaxon.molecule

import org.squonk.types.MoleculeObject
import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule

/**
 * Created by timbo on 14/04/2014.
 */
class MoleculeUtilsSpec extends Specification {


    def 'heavy atom counter'() {
        
        expect:
        MoleculeUtils.heavyAtomCount(MolImporter.importMol(smiles)) == counts

        where:
        smiles << ['c1ccccc1', 'c1ccncc1', 'CCCC', 'C[H]']
        counts << [6, 6, 4, 1]
    }

    
    def 'parent molecule'() {
        
        expect:
        
        smiles == expected
        
        where:
        
        smiles << [
            parentAsSmiles('c1ccccc1'),
            parentAsSmiles('CCC'),
            parentAsSmiles('CCC.c1ccccc1'),
            parentAsSmiles('c1ccccc1.CCC'),
            parentAsSmiles('c1ccccc1.CCC.[Na+]'),
            parentAsSmiles('CCC.CCC'),
            parentAsSmiles('CCC.CNC'),
            parentAsSmiles('[H][H].CNC'),
            parentAsSmiles('[H][H].[H]')
        ]
                
        expected << [
            'c1ccccc1', 
            'CCC',
            'c1ccccc1',
            'c1ccccc1',
            'c1ccccc1',
            'CCC',
            'CCC',
            'CNC',
            '[H][H]'
        ]
    }
    
    def parentAsSmiles(def input) {
        Molecule mol = MolImporter.importMol(input, 'smiles')
        Molecule parent = MoleculeUtils.findParentStructure(mol)
        String smi = MolExporter.exportToFormat(parent, 'smiles')
        return smi
    }

    def "concatenate molecule streams"() {

        List mols1 = [
                new MoleculeObject("C", "smiles", [a:1]),
                new MoleculeObject("CC", "smiles", [a:2]),
                new MoleculeObject("CCC", "smiles", [a:3])
        ]

        List mols2 = [
                new MoleculeObject("CCCC", "smiles", [a:4]),
                new MoleculeObject("CCCCC", "smiles", [a:5]),
                new MoleculeObject("CCCCCC", "smiles", [a:6]),
                new MoleculeObject("CCCCCCC", "smiles", [a:7])
        ]

        when:
        def is = MoleculeUtils.concatenateMoleculeStreams("sdf", null, mols1.stream(), mols2.stream())
        def text = is.text

        then:
        text.split("M  END").size() == 8


    }


    def "concatenate reaction and stream as mrv"() {

        List mols = [
                new MoleculeObject("C", "smiles", [a:1]),
                new MoleculeObject("CC", "smiles", [a:2]),
                new MoleculeObject("CCC", "smiles", [a:3])
        ]

        Molecule mol = MolImporter.importMol("CCCCC>>CCC")

        when:
        def is = MoleculeUtils.concatenateMoleculeStreams("mrv", mol, mols.stream())
        def text = is.text

        then:
        text.split("<MDocument>").size() == 5


    }
        
  
}
