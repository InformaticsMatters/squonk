package org.squonk.types.io

import org.squonk.dataset.*
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

import java.util.stream.Stream

/**
 *
 * @author timbo
 */
class JsonHandlerSpec extends Specification {

    void "write simple types to json"() {

        expect:
        JsonHandler.instance.objectToJson(value) == json

        where:
        value | json
        'Hello'           | '"Hello"'   // gets quoted
        new Integer(123)  | '123'
        new Float(123.4)  | '123.4'
        new Double(123.4) | '123.4'
        true              | 'true'
        false             | 'false'
    }

    void "read simple types from json"() {

        expect:
        JsonHandler.instance.objectFromJson(json, cls) == value

        where:
        value | json | cls
        'Hello'           | '"Hello"' | String.class
        //'Hello'           | 'Hello'   | String.class
        new Integer(123)  | '123'     | Integer.class
        new Float(123.4)  | '123.4'   | Float.class
        new Double(123.4) | '123.4'   | Double.class
        true              | 'true'    | Boolean.class
        false             | 'false'   | Boolean.class
    }
    
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

    void "slow producer"() {
        int count = 100

        when:
        InputStream input = JsonHandler.getInstance().marshalStreamToJsonArray(Stream.generate() {
            sleep(5)
            new MoleculeObject("C", "smiles")
        }.limit(count), false)

        Dataset<MoleculeObject> results = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), input)

        then:
        results.items.size() == count


    }


}

