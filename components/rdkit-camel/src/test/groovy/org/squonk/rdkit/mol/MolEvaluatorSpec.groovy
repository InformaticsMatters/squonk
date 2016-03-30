package org.squonk.rdkit.mol

import spock.lang.Specification
import org.RDKit.RWMol

/**
 *
 * @author timbo
 */
class MolEvaluatorSpec extends Specification {
	
    
    void "calc logp"() {
        
        when:
        def logp = MolEvaluator.calculate(RWMol.MolFromSmiles('CCCC'), EvaluatorDefintion.Function.LOGP)
        
        then:
        logp != null
        logp instanceof Float
    }
}

