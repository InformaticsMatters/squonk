package com.im.lac.chemaxon.io

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeObjectIteratorImplSpec extends Specification {
    
    def "read smiles"() {
        setup:
        File file = new File("../../data/testfiles/nci1000.smiles")
        MoleculeObjectIterableImpl impl = new MoleculeObjectIterableImpl(file)
        
        when:
        def mols = impl.collect()
        
        then:
        mols.size() == 1000
        
    }
    
    def "read sdf"() {
        setup:
        File file = new File("../../data/testfiles/dhfr_standardized.sdf.gz")
        MoleculeObjectIterableImpl impl = new MoleculeObjectIterableImpl(file)
        
        when:
        def mols = impl.collect()
        
        then:
        mols.size() == 756
        mols.each { mo ->
            assert mo.getSourceAsString() != null
            assert mo.getValue('set') != null
        }
        
    }
	
}

