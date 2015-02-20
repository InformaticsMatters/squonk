package com.im.lac.chemaxon.molecule

import com.im.lac.types.MoleculeObjectIterable
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeObjectFactorySpec extends Specification {
    
    def "read smiles"() {
        setup:
        File file = new File("../../data/testfiles/nci1000.smiles")
        MoleculeObjectIterable impl = MoleculeObjectUtils.createIterable(file)
        
        when:
        def mols = impl.collect()
        
        then:
        mols.size() == 1000
        mols.each { mo ->
            assert "smiles" == mo.format
        }
        
    }
    
    def "read sdf"() {
        setup:
        File file = new File("../../data/testfiles/dhfr_standardized.sdf.gz")
        MoleculeObjectIterable impl = MoleculeObjectUtils.createIterable(file)
        
        when:
        def mols = impl.collect()
        
        then:
        mols.size() == 756
        mols.each { mo ->
            assert mo.getSource() != null
            assert mo.getValue('set') != null
        }
        
    }
	
}

