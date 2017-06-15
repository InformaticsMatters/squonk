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

package org.squonk.reader

import org.squonk.types.MoleculeObject
import org.squonk.util.IOUtils
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
        int names = 0
        while (reader.hasNext()) {
            MoleculeObject mo = reader.next()
            count++
            if (mo.getValue("name") != null) {
                names++
            }
        }
        
        then:
        //println "found $count items"
        count == 756
        //println "found $names names"
        names == 756
        
        cleanup:
        reader.close()
        
    }
    
    void "change name field"() {
        FileInputStream is = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        SDFReader reader = new SDFReader(IOUtils.getGunzippedInputStream(is))
        reader.setNameFieldName("banana")
        
        when:
        int count = 0
        int names = 0
        int props = 0
        while (reader.hasNext()) {
            MoleculeObject mo = reader.next()
            count++
            props += mo.getValues().size()
            if (mo.getValue("banana") != null) {
                names++
            }
        }
        
        then:
        //println "found $count items"
        count == 756
        //println "found $names bananas"
        names == 756
        //println "found $props pros"
        props == 5292
        
        cleanup:
        reader.close()
        
    }
    
     void "no name field"() {
        FileInputStream is = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        SDFReader reader = new SDFReader(IOUtils.getGunzippedInputStream(is))
        reader.setNameFieldName(null)
        
        when:
        int count = 0
        int props = 0
        while (reader.hasNext()) {
            MoleculeObject mo = reader.next()
            count++
            props += mo.getValues().size()
        }
        
        then:
        //println "found $count items"
        count == 756
        //println "found $props props"
        props == 4536
        
        cleanup:
        reader.close()
        
    }
    
    
     void "as stream"() {
        FileInputStream is = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        SDFReader reader = new SDFReader(IOUtils.getGunzippedInputStream(is))
        
        when:
        long count = reader.asStream().count()
        
        then:
        //println "found $count items"
        count == 36
        
        cleanup:
        reader.close()
    }

    void "multiple bank lines issue"() {
        FileInputStream is = new FileInputStream("../../data/testfiles/buggy_with_multiple_blank_lines.sdf.gz")
        SDFReader reader = new SDFReader(IOUtils.getGunzippedInputStream(is))

        when:
        long count = reader.asStream().count()

        then:
        //println "found $count items"
        count == 11

        cleanup:
        reader.close()
    }

    void "read empty"() {
        SDFReader reader = new SDFReader(new ByteArrayInputStream(new byte[0]))

        when:
        long count = reader.asStream().count()

        then:
        count == 0

        cleanup:
        reader.close()
    }


}

