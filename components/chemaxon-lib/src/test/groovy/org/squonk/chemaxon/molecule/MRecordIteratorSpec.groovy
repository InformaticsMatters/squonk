package org.squonk.chemaxon.molecule

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MRecordIteratorSpec extends Specification {
    
    def "read sdf"() {
        setup:
        File file = new File("../../data/testfiles/dhfr_standardized.sdf.gz")
        MRecordIterator impl = new MRecordIterator(new FileInputStream(file))
        
        when:
        def recs = impl.collect()
        
        
        then:
        recs.size() == 756
        recs.each { rec ->
            assert rec.propertyContainer.getObject('set') != null
        }      
        
        cleanup:
        impl.close() 
    }
    
    
    def "multiple hasNext()"() {
        setup:
        File file = new File("../../data/testfiles/dhfr_standardized.sdf.gz")
        MRecordIterator impl = new MRecordIterator(new FileInputStream(file))
        
        when:
        int count = 0
        while (impl.hasNext()) {
            impl.hasNext() // called again, should not affect results
            impl.next()
            count++
        }
        
        then:
        count == 756
        
        cleanup:
        impl.close() 
    }
    
    
    def "multiple next() without hasNext()"() {
        setup:
        File file = new File("../../data/testfiles/dhfr_standardized.sdf.gz")
        MRecordIterator impl = new MRecordIterator(new FileInputStream(file))
        
        when:
        int count = 0
        while  (count < 756) {
            impl.next()
            count++
        }
        
        then:
        !impl.hasNext()
        
        cleanup:
        impl.close() 
    }
   
	
}

