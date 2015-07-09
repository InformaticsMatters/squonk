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
	
    
    void "find rdkitmol smiles"() {

        when:
        def rdkitmol = MolReader.findROMol(new MoleculeObject('CCCC', "smiles"))
        
        then:
        rdkitmol != null
    }
    
    void "test generate rdkitmol smiles"() {
        
        when:
        def rdkitmol = MolReader.generateMolFromString('CCCC', "smiles")
        
        then:
        rdkitmol != null
    }
    
    void "test generate rdkitmol molblock"() {
        
        when:
        def rdkitmol = MolReader.generateMolFromString('''\

  Mrv0541 07071512282D          

  2  1  0  0  0  0            999 V2000
   -2.9391    1.7237    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -2.2246    2.1362    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
  1  2  1  0  0  0  0
M  END
''', "mol")
        
        then:
        rdkitmol != null
    }
    
    void "calc logp"() {
        
        when:
        def logp = MolEvaluator.calculate(RWMol.MolFromSmiles('CCCC'), EvaluatorDefintion.Functions.LOGP)
        
        then:
        logp != null
        logp instanceof Double
    }
}

