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

package org.squonk.types.io

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.ContextAttributes
import com.fasterxml.jackson.databind.module.SimpleModule
import org.squonk.types.BasicObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class BasicObjectJsonDeserializerSpec extends Specification {
	
    void "test simple unmarshal"() {
        
        String json = '{"uuid":"e5c7aff8-1512-43b7-ac26-693322999422","values":{"two":2,"one":1}}'
        
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(BasicObject.class, new BasicObjectJsonDeserializer())
        mapper.registerModule(module)
        
        when:
        BasicObject bo = mapper.readerFor(BasicObject.class).readValue(json)
        
        then:
        bo != null
        bo.values['one'] == 1
    }
    
    void "test simple unmarshal with mappings"() {
        
        String json = '{"uuid":"e5c7aff8-1512-43b7-ac26-693322999422","values":{"two":2.2,"one":1}}'
        
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(BasicObject.class, new BasicObjectJsonDeserializer())
        mapper.registerModule(module)
        Map<String,Class> mappings = [one:Integer.class,two:BigDecimal.class]
        
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS, mappings)
        
        when:
        BasicObject bo = mapper.readerFor(BasicObject.class).with(attrs).readValue(json)
        
        then:
        bo != null
        bo.values['one'] instanceof Integer
        bo.values['one'] == 1
        bo.values['two'] instanceof BigDecimal
        bo.values['two'] == 2.2
    }


    void "test simple unmarshal with array value"() {

        String json = '{"uuid":"e5c7aff8-1512-43b7-ac26-693322999422","values":{"one":"hello","two":["hello","world"], "three":"three"}}'

        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(BasicObject.class, new BasicObjectJsonDeserializer())
        mapper.registerModule(module)


        when:
        BasicObject bo = mapper.readerFor(BasicObject.class).readValue(json)

        then:
        bo != null
        bo.values['one'] instanceof String
        bo.values['one'] == "hello"
        bo.values['two'] instanceof List
        bo.values['two'].size() == 2
        bo.values['three'] == "three"
    }

}

