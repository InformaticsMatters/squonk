/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

