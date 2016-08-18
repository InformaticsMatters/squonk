package org.squonk.dataset

import com.fasterxml.jackson.databind.ObjectReader
import org.squonk.types.BasicObject
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetSpec extends Specification {
    
    static List objects = [
        new BasicObject([one:1, two:2.2, three: new Date()]), 
        new BasicObject([one:4, two:5.5]), 
        new BasicObject([one:6, two:7.7])]
    
    static DatasetMetadata metadata = new DatasetMetadata(BasicObject.class, ["one":Integer.class, "two":Integer.class], 2)
    
     
    void "stream to list"() {
        
        setup:
        Dataset ds = new Dataset(BasicObject.class,objects.stream())
        
        when:
        ds.getItems()
        
        then:
        ds.items.size() == 3
        // getting the items will have consumed the stream and it be set to null
        ds.@stream == null
    }
    
    void "inputstream to list"() {
        
        setup:
        Dataset ds = new Dataset(BasicObject.class,
            new ByteArrayInputStream('[{"uuid":"4fe54df5-265c-4627-9a1c-69f6279944f0","values":{"two":2,"one":1}},{"uuid":"8a9478be-99bc-47bb-9273-3a54943dc1e2","values":{"two":22,"one":11}}]'.bytes),
            metadata)
        
        when:
        ds.getItems()
        
        then:
        ds.items.size() == 2
        //println ds.items
        // getting the items will have consumed the stream and it be set to null
        ds.@stream == null
        ds.@inputStream == null
    }
    
    void "json to iterator"() {
        
        println "json to iterator()"
        String json ='[{"uuid":"4fe54df5-265c-4627-9a1c-69f6279944f0","values":{"two":2,"one":1}},{"uuid":"8a9478be-99bc-47bb-9273-3a54943dc1e2","values":{"two":22,"one":11}}]'
        ObjectReader reader = JsonHandler.getInstance().getObjectMapper().reader().forType(BasicObject.class)
        
        when:
        Iterator iter = reader.readValues(json.bytes)
        int count = 0
        while (iter.hasNext()) {
            count++
            iter.next()
        }
        
        then:
        count == 2
    }
    
    void "generate metadata from stream"() {
        
        setup:
        Dataset ds = new Dataset(BasicObject.class, objects.stream())
        
        when:
        ds.generateMetadata()
        
        then:
        ds.metadata != null
        ds.metadata.size == 3
        ds.list.size() == 3
        ds.@stream == null
    }

    void "not present field meta removed"() {

        setup:
        DatasetMetadata meta = new DatasetMetadata(BasicObject.class)
        meta.putFieldMetaProp("notpresent", "foo", "bar")
        Dataset ds = new Dataset(BasicObject.class, objects.stream(), meta)

        when:
        ds.generateMetadata()

        then:
        !ds.metadata.fieldMetaPropsMap.containsKey("notpresent")
    }
    
    void "can't read stream twice"() {
        
        setup:
        Dataset ds = new Dataset(BasicObject.class, objects.stream())
        ds.getStream()
        
        when:
        ds.getStream()
        
        then:
        thrown(IllegalStateException)
    }
    
    void "url to list"() {
        println "url to list()"
        
        when:
        URL url = new URL("file:src/test/groovy/org/squonk/dataset/objects.json")
        Dataset ds = new Dataset(BasicObject.class, url, metadata)
        
        then:
        ds.items.size() == 2
        ds.getStream().count()
        ds.getStream().count()
    }


}