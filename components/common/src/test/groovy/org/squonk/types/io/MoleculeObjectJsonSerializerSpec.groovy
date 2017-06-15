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
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SequenceWriter
import com.fasterxml.jackson.databind.cfg.ContextAttributes
import com.fasterxml.jackson.databind.module.SimpleModule
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeObjectJsonSerializerSpec extends Specification {
    
   
    
    void "serialize single"() {
        
        setup:
        MoleculeObject mo = new MoleculeObject("smiles")
        mo.putValue("integer", new Integer(0))
        mo.putValue("biginteger", new BigInteger(0))
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(MoleculeObject.class, new MoleculeObjectJsonSerializer())
        mapper.registerModule(module)
        Map mappings = [:]
        
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS, mappings)
        ObjectWriter writer = mapper.writerFor(MoleculeObject.class).with(attrs)
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        
        when:
        writer.writeValue(out, mo)
        String json = new String(out.toByteArray())
            
        then:
        println "JSON: " + json
        println "MAPPINGS: " + mappings
        json != null
        mappings.size() == 2
        
    }
    
    void "serialize multiple"() {
        
        setup:
        def mols = [new MoleculeObject("smiles1"), new MoleculeObject("smiles1")]
        mols[0].putValue("integer", new Integer(0))
        mols[0].putValue("biginteger", new BigInteger(0))
        mols[1].putValue("integer", new Integer(1))
        mols[1].putValue("biginteger", new BigInteger(1))
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(MoleculeObject.class, new MoleculeObjectJsonSerializer())
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer())
        mapper.registerModule(module)
        Map mappings = [:]
        
        ContextAttributes attrs1 = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS, mappings)
        
        ObjectWriter writer = mapper.writerFor(MoleculeObject.class).with(attrs1)
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        SequenceWriter sw = writer.writeValuesAsArray(out);
        
        when:
        mols.each {
            sw.write(it)
        }
        sw.close()

        String json = new String(out.toByteArray())
        //println "JSON: " + json
        //println "META: " + meta
        
        ContextAttributes attrs2 = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS, mappings)
        ObjectReader reader = mapper.reader(MoleculeObject.class).with(attrs2)
        Iterator result = reader.readValues(json)
        def mols2 = result.collect {
            it
        }
            
        then:
        json != null
        
        mols2.size() == 2
        mols2[0].getValue("integer") instanceof Integer
        mols2[0].getValue("integer") == 0
        mols2[0].getValue("biginteger") instanceof BigInteger
        mols2[0].getValue("biginteger") == 0        
    }
	
}

