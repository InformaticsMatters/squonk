package com.im.lac.rdkit.mol

import java.util.stream.*
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
        when:
        List<ROMol> mols = MolReader.readSmiles('../../data/testfiles/nci100.smiles', "\t", 0, 1, false, true)
            .collect(Collectors.toList())
        
        then:
        mols.size() == 100
        mols.each {
            assert it != null
        }
        
    }
	
}

