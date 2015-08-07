package com.im.lac.camel.rdkit

import com.im.lac.types.MoleculeObject
import java.util.stream.*
import spock.lang.Specification

import static com.im.lac.rdkit.mol.EvaluatorDefintion.Function.*


/**
 *
 * @author timbo
 */
class RDKitMoleculeProcessorSpec extends Specification {
    
    void "simple calc logp"() {
        
        setup:
        println "simple calc logp()"
        RDKitMoleculeProcessor p = new RDKitMoleculeProcessor()
        p.calculate('logp', LOGP)
        def mols = Stream.of(new MoleculeObject("C", "smiles"), new MoleculeObject("CC", "smiles"))
        
        
        when: 
        Stream<MoleculeObject> result = p.evaluate(null, mols, p.definitions)
        def list = result.collect(Collectors.toList())
        
        then:
        list.size() == 2
        list[0].getValue("logp") != null
        list[1].getValue("logp") != null
        
    }
	
}

