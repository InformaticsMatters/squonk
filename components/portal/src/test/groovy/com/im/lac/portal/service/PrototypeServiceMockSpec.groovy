package com.im.lac.portal.service

import com.im.lac.portal.service.mock.PrototypeServiceMock
import spock.lang.Specification

class PrototypeServiceMockSpec extends Specification {
    

    
    def "test createDataset from sdfile"() {

        when:
        def is = new FileInputStream('../../data/testfiles/dhfr_standardized.sdf.gz')
        def mock = null
        // can't handle as Float as there are modified (>) symbols present.
        // needs QualifiedValue adding to handle this
        //def config = [PC_uM: Float.class, TG_uM: Float.class, RL_uM: Float.class, set: Integer.class]
        def config = [set: Integer.class]
        try {
            def service = new PrototypeServiceMock()
            mock = service.parseSdf(null, is, config)
        } finally {
            is.close()
        }
        
        then:
        mock != null
        mock.getDatasetRowList().size() == 756
        
        
    }
    

}
