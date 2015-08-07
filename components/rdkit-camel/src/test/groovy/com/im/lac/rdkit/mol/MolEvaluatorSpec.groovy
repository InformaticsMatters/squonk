package com.im.lac.rdkit.mol

import com.im.lac.types.MoleculeObject
import spock.lang.Specification
import org.RDKit.ROMol
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
        logp instanceof Double
    }
}

