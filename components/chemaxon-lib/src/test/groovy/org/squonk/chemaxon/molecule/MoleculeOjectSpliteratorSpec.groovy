package org.squonk.chemaxon.molecule

import org.squonk.types.MoleculeObject
import java.util.stream.Stream
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeOjectSpliteratorSpec extends Specification {
    
    void "simple read"() {
        setup:
        FileInputStream file = new FileInputStream("../../data/testfiles/nci100.smiles")
        Stream<MoleculeObject> stream = new MoleculeObjectSpliterator(file).asStream(false)
        
        when:
        def count = stream.count()
        
        then:
        count == 100
        
        cleanup:
        file.close()
        
    }
	
}

