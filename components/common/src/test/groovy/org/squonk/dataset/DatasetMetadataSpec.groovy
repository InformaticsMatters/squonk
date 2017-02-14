package org.squonk.dataset

import org.squonk.types.BasicObject
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 18/08/16.
 */
class DatasetMetadataSpec extends Specification {

    void "to/from json"() {

        def meta1 = new DatasetMetadata(BasicObject.class,
                [foo:String.class, bar:Integer.class], // class mappings
                [ // field props
                        new DatasetMetadata.PropertiesHolder('foo', [prop1: 'fooprop', num:new BigInteger(100)]),
                        new DatasetMetadata.PropertiesHolder('bar', [prop2: 'barprop', num:new BigInteger(1000)])
                ],
                100, // size
                [simple:'property', bigint:new BigInteger(1000)] // dataset props
        )

        when:
        def json = JsonHandler.instance.objectToJson(meta1)
        println json
        def meta2 = JsonHandler.instance.objectFromJson(json, DatasetMetadata.class)

        then:
        meta2.getProperty('simple') == 'property'
        meta2.getProperty('bigint') instanceof BigInteger
        meta2.getFieldMetaProp('foo', 'prop1')== 'fooprop'
        meta2.getFieldMetaProp('bar', 'prop2')== 'barprop'
        meta2.getValueClassMappings().get('foo') == String.class
        meta2.size == 100
    }


    
}
