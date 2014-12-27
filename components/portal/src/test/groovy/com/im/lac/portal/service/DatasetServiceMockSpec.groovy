package com.im.lac.portal.service

import com.im.lac.portal.service.mock.DatasetServiceMock
import com.im.lac.portal.service.api.DatasetDescriptor
import spock.lang.Specification

class DatasetServiceMockSpec extends Specification {
    

    
    def "test createDataset from sdfile"() {

        when:
        def is = new FileInputStream('../../data/testfiles/dhfr_standardized.sdf.gz')
        DatasetDescriptor descriptor = null
        // can't handle as Float as there are modified (>) symbols present.
        // needs QualifiedValue adding to handle this
        //def config = [PC_uM: Float.class, TG_uM: Float.class, RL_uM: Float.class, set: Integer.class]
        def config = [set: Integer.class]
        try {
            def service = new DatasetServiceMock()
            descriptor = service.importFromStream(null, is, config)
        } finally {
            is.close()
        }
        
        then:
        descriptor != null
        descriptor.listAllRowDescriptors().size() == 1
        descriptor.listAllRowDescriptors()[0].listAllPropertyDescriptors().size() == 7
        // unsure how to get the rows
        //mock.getRowList().size() == 756
        
        
    }
    

}
