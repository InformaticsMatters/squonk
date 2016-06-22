package org.squonk.rdkit.mol

import org.squonk.types.MoleculeObject
import java.util.stream.*
import org.RDKit.ROMol
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
    
    void "test generate rdkitmol smiles format not specified"() {
        
        when:
        def rdkitmol = MolReader.generateMolFromString('CCCC', null)
        
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
}