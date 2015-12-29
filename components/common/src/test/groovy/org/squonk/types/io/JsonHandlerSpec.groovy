package org.squonk.types.io

import com.im.lac.types.*
import org.squonk.dataset.*
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class JsonHandlerSpec extends Specification {
    
    void "generateJsonForDataset"() {
        println "generateJsonForDataset()"
        Dataset ds = new Dataset(BasicObject.class, DatasetSpec.objects)
        
        when:
        InputStream is = JsonHandler.getInstance().marshalStreamToJsonArray(ds.getStream(), false)
        String json = is.text
        println json
        
        then:
        json.length() > 0
    }
    
    
    void "read Dataset from json"() {
        
        println "json to iterator()"
        String json ='[{"uuid":"4fe54df5-265c-4627-9a1c-69f6279944f0","values":{"two":2,"one":1}},{"uuid":"8a9478be-99bc-47bb-9273-3a54943dc1e2","values":{"two":22,"one":11}}]'
        DatasetMetadata metadata = new DatasetMetadata(BasicObject.class, ["one":Integer.class, "two":Integer.class], 2)
        Dataset ds = JsonHandler.getInstance().unmarshalDataset(metadata, new ByteArrayInputStream(json.bytes))
        
        when:
        List items = ds.items
        
        then:
        items.size() == 2
        ds.metadata.size == 2
    }
	
}

