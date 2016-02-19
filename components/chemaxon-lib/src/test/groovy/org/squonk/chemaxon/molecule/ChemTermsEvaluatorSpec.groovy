package org.squonk.chemaxon.molecule

import com.im.lac.types.MoleculeObject
import org.squonk.reader.SDFReader
import spock.lang.Specification
import chemaxon.formats.MolImporter

import java.util.stream.Stream
import java.util.zip.GZIPInputStream

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


    def 'ChemTerms calc for Stream'() {

        List mols = [
                new MoleculeObject("C", "smiles"),
                new MoleculeObject("CC", "smiles"),
                new MoleculeObject("CCC", "smiles"),
                new MoleculeObject("CCCC", "smiles")
        ]

        def atomCountLt6 = new ChemTermsEvaluator('atoms', 'atomCount()<6')


        when:
        Stream s = mols.stream().map() {
            def result = atomCountLt6.processMoleculeObject(it)
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
        def atomCountLt6 = new ChemTermsEvaluator('atoms', 'atomCount()<6')


        when:
        Stream s = reader.asStream().map() {
            def result = atomCountLt6.processMoleculeObject(it)
            //println result
        }

        long count = s.count()


        then:
        count == 36

        cleanup:
        reader?.close()
    }




}
