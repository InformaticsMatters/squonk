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
