package com.im.lac.rdkit.mol

import java.util.stream.Stream
import org.RDKit.ROMol
import org.RDKit.SmilesMolSupplier
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MolReaderSpec extends Specification {
    
    void "test read smiles string"() {
        when:
        def mol = MolReader.generateMolFromString('CCCC', 'smiles')
        
        then:
        mol != null
    }
    
    void "test read smiles file"() {
        setup:
        
        when:
        Stream<ROMol> mols = MolReader.readSmiles('../../data/testfiles/nci100.smiles', "\t", 1, 2, false, true)
        long count = mols.count()
        
        then:
        count == 100
        
    }
	
}

