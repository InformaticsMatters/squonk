package org.squonk.reader

import com.im.lac.types.BasicObject
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
        CSVReader reader = new CSVReader(is, format)
        
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
        CSVReader reader = new CSVReader(is, format)
        
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
        CSVReader reader = new CSVReader(is, format)
        
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
        CSVReader reader = new CSVReader(is, format)
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 3
        list[0].values.size() == 3
    }
    
    void "simple tab reader"() {
        //println "simple tab reader"
        InputStream is = new FileInputStream("../../data/testfiles/Pubchem.tab.gz")
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withDelimiter('\t' as char)
        CSVReader reader = new CSVReader(is, format)
        
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
        CSVReader reader = new CSVReader(is, format)
        
        when:
        Stream<BasicObject> stream = reader.asStream()
        def list = stream.collect(Collectors.toList())
        
        then:
        list.size() == 4
        list[0].values.size() == 3
    }
    
}

