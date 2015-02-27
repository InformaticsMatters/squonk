package com.im.lac.camel.rdkit

import com.im.lac.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class RDKitMoleculeProcessorSpec extends Specification {
    
    void "simple echo"() {
        setup:
        RDKitMoleculeProcessor p = new RDKitMoleculeProcessor()
        p.calculate('logp', 'logP()')
        def mols = [new MoleculeObject("C"), new MoleculeObject("CC")]
        
        
        when: 
        Iterator<MoleculeObject> result = p.evaluate(null, mols.iterator(), p.definitions)
        
        then:
        result.collect().size() == 2
        
    }
	
}

