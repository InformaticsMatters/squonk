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
class BasicObjectJsonSerializerSpec extends Specification {
	
    void "test simple marshal"() {
        
        BasicObject bo = new BasicObject([one:1,two:2])
        
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(BasicObject.class, new BasicObjectJsonSerializer())
        mapper.registerModule(module)
        
        when:
        String json = mapper.writer().writeValueAsString(bo)
        
        then:
        json != null
    }
    
    void "test generate mappings"() {
        
        BasicObject bo = new BasicObject([one:1,two:2.2])
        Map mappings = [:]
        
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(BasicObject.class, new BasicObjectJsonSerializer())
        mapper.registerModule(module)
        
        when:
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS, mappings)
        String json = mapper.writer().with(attrs).writeValueAsString(bo)
        
        then:
        json != null
        mappings['one'] == Integer.class
        mappings['two'] == BigDecimal.class

    }
    
    
}

