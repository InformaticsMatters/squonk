package com.im.lac.camel.rdkit

import com.im.lac.types.MoleculeObject
import java.util.stream.Stream
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
        def mols = Stream.of(new MoleculeObject("C"), new MoleculeObject("CC"))
        
        
        when: 
        Stream<MoleculeObject> result = p.evaluate(null, mols, p.definitions)
        
        then:
        result.count() == 2
        
    }
	
}

