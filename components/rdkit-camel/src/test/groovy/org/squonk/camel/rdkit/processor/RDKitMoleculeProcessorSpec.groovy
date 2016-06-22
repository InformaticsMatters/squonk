package org.squonk.camel.rdkit.processor

import org.squonk.types.MoleculeObject
import org.squonk.util.ExecutionStats

import java.util.stream.*
import spock.lang.Specification

import static org.squonk.rdkit.mol.EvaluatorDefintion.Function.*


/**
 *
 * @author timbo
 */
class RDKitMoleculeProcessorSpec extends Specification {
    
    void "simple calc logp"() {
        
        setup:
        println "simple calc logp()"
        RDKitMoleculeProcessor p = new RDKitMoleculeProcessor()
        p.calculate(LOGP, 'logp')
        def mols = Stream.of(new MoleculeObject("C", "smiles"), new MoleculeObject("CC", "smiles"))
        
        
        when: 
        Stream<MoleculeObject> result = p.evaluate(null, mols, p.definitions, new ExecutionStats())
        def list = result.collect(Collectors.toList())
        
        then:
        list.size() == 2
        list[0].getValue("logp") != null
        list[1].getValue("logp") != null
        
    }
	
}

