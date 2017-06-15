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

import org.squonk.types.BasicObject
import java.util.stream.*

import org.apache.commons.csv.CSVFormat
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CSVReaderSpec extends Specification {
    
    static String CSV1 = '''\
field1,field2,field3
1,one,uno
2,two,duo
3,three,tres'''
    
    static String SPACE1 = '''\
field1 field2 field3
1 one uno
2 two duo
3 three tres'''
    
    void "simple csv reader with header"() {
        //println "simple csv reader with header"
        InputStream is = new ByteArrayInputStream(CSV1.bytes)
        CSVFormat format = CSVFormat.DEFAULT.withHeader()
        CSVReader reader = new CSVReader(is, format, '')
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 3
        list[0].values.size() == 3
    }
    
    void "simple csv reader no header"() {
        //println "simple csv reader no header"
        InputStream is = new ByteArrayInputStream(CSV1.bytes)
        CSVFormat format = CSVFormat.DEFAULT
        CSVReader reader = new CSVReader(is, format, '')
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 4
        list[0].values.size() == 3
    }
    
    void "simple csv reader specified header"() {
        //println "simple csv reader specified header"
        InputStream is = new ByteArrayInputStream(CSV1.bytes)
        CSVFormat format = CSVFormat.DEFAULT.withHeader("X1","X2","X3")
        CSVReader reader = new CSVReader(is, format, '')
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 4
        list[0].values.size() == 3
    }
    
    void "simple csv reader specified header skip first row"() {
        //println "simple csv reader specified header skip first row"
        InputStream is = new ByteArrayInputStream(CSV1.bytes)
        CSVFormat format = CSVFormat.DEFAULT.withHeader("X1","X2","X3").withSkipHeaderRecord()
        CSVReader reader = new CSVReader(is, format, '')
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 3
        list[0].values.size() == 3
    }
    
    void "pubchem tab reader"() {
        //println "pubchem tab reader"
        InputStream is = new FileInputStream("../../data/testfiles/Pubchem.tab.gz")
        CSVFormat format = CSVFormat.TDF.withHeader()
        CSVReader reader = new CSVReader(is, format, '')
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 463
        list[0].values.size() == 8
        
        cleanup:
        reader.close()
    }
	
    
    void "single space reader with header"() {
        //println "single space reader with header"
        InputStream is = new ByteArrayInputStream(SPACE1.bytes)
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(' ' as char)
        CSVReader reader = new CSVReader(is, format, '')
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 4
        list[0].values.size() == 3
    }

    void "kinase tab reader"() {
        //println "kinase tab reader"
        InputStream is = new FileInputStream("../../data/testfiles/Nature-SAR-100.txt.gz")
        CSVFormat format = CSVFormat.TDF.withHeader()
        CSVReader reader = new CSVReader(is, format, '')

        when:
        Stream<BasicObject> stream = reader.asStream()
        def size = stream.count()

        then:
        size == 100

        cleanup:
        reader.close()
    }
    
}

