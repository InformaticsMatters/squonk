package com.im.lac.services.dataset.service

import com.im.lac.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetHandlerSpec extends Specification {
    
    void "test json generation"() {
        setup:
        DatasetHandler dh = new DatasetHandler(null, '/tmp/foo')
        def mols = [new MoleculeObject('C'), new MoleculeObject('CC')]
        
        when:
        def holder = dh.generateJsonForItem(mols.stream(), false)
        def json = holder.inputStream.text
        //holder.inputStream.close()
        
        then:
        holder.metadata.size == 2
        
    }
	
}

