package org.squonk.chemaxon.molecule

import chemaxon.formats.MdlCompressor
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

    def "decompress molfile"() {

        when:
        String compressed = "\n  ChemDraw02121414022D\n\n 44 47  0  0  0  0  0  0  0  0999 V2000\n1eVVbhDX60\nylyViONX60\nylyVcntX60\n0vYVHz7Y60\nFl5Vk0xX60\nFl5VqdQX60\nKdeUSSAX70\nKdeUP5gW60\nOiEU2fMW60\nOiEUhHsV80\n9shU+MfV60\nKdeUO+8V80\n6W2VaZrU60\n1eVV6W2V60\nqWvVRKoU60\nylyVNzHU60\nttPWzF8U80\ne1tWngRU60\nwVzWiouU60\nqdQXFl5V60\noHnXaZrU60\nf2kXWCLU80\nkwGXzF8U60\nkwGXNtdT60\nWpgXhhNT80\n1eVV815U80\nFl5VWCLU60\nKdeU815U60\nKdeUEeaT80\n0vYVGscW80\n6W2VjvPW60\nFl5VpWvV60\n0vYVylyV80\nttPWKD7X60\nVopWt9KX80\nTanTGscW60\n0vYVLKeY80\n+MfVlQSV80\ngZmWcBPV80\nttPWHz7Y80\nZREY6W2V80\nKdeUQCBY80\nnAGWO+8V80\n66XX98cV80\n10202\n10601\n20301\n20Y01\n30401\n30e01\n40501\n40b01\n50601\n50g01\n60701\n70801\n80901\n80V01\n90A01\n90a01\nA0B01\nB0C01\nB0W01\nC0D01\nD0E01\nD0R01\nE0F01\nE0c01\nF0G01\nF0h01\nG0H01\nG0Q01\nH0I01\nI0J01\nI0N01\nJ0K01\nJ0d01\nK0L01\nK0i01\nL0M01\nL0f01\nM0N01\nN0O01\nO0P01\nQ0R01\nR0S01\nS0T01\nU0V01\nV0W01\nW0X01\nY0Z01\nM  END\n"

        //String uncompressed = MoleculeUtils.decompressMolfile(compressed)
        String uncompressed = MdlCompressor.convert(compressed, MdlCompressor.DECOMPRESS)

        then:
        uncompressed.length() > compressed.length()
        !MolImporter.importMol(uncompressed).isEmpty()
    }
        
  
}
