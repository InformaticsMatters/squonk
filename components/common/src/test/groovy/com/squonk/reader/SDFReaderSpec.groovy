package com.squonk.reader

import com.im.lac.types.MoleculeObject
import com.squonk.util.IOUtils
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class SDFReaderSpec extends Specification {
    
    void "read sdf"() {
        FileInputStream is = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        SDFReader reader = new SDFReader(IOUtils.getGunzippedInputStream(is))
        
        when:
        int count = 0
        while (reader.hasNext()) {
            MoleculeObject mo = reader.next()
            count++
        }
        
        then:
        count == 756
        println "found $count items"
        
        cleanup:
        reader.close()
        
    }
	
}

