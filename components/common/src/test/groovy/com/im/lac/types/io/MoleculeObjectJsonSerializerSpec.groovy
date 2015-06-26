package com.im.lac.types.io

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SequenceWriter
import com.fasterxml.jackson.databind.cfg.ContextAttributes
import com.fasterxml.jackson.databind.module.SimpleModule
import com.im.lac.types.MoleculeObject
import com.im.lac.dataset.Metadata
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
        Metadata meta = new Metadata()
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(MoleculeObject.class, new MoleculeObjectJsonSerializer())
        mapper.registerModule(module)
        
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_METADATA, meta)
        ObjectWriter writer = mapper.writerFor(MoleculeObject.class).with(attrs)
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        
        when:
        writer.writeValue(out, mo)
        String json = new String(out.toByteArray())
            
        then:
        println "JSON: " + json
        println "META: " + meta
        json != null
        meta.getPropertyTypes().size() == 2
        
    }
    
    void "serialize multiple"() {
        
        setup:
        def mols = [new MoleculeObject("smiles1"), new MoleculeObject("smiles1")]
        mols[0].putValue("integer", new Integer(0))
        mols[0].putValue("biginteger", new BigInteger(0))
        mols[1].putValue("integer", new Integer(1))
        mols[1].putValue("biginteger", new BigInteger(1))
        Metadata meta = new Metadata(MoleculeObject.class.getName(), Metadata.Type.ARRAY, 2)
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addSerializer(MoleculeObject.class, new MoleculeObjectJsonSerializer())
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer())
        mapper.registerModule(module)
        
        ContextAttributes attrs1 = ContextAttributes.getEmpty().withSharedAttribute("metadata", meta)
        
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
        
        ContextAttributes attrs2 = ContextAttributes.getEmpty()
        .withSharedAttribute(JsonHandler.ATTR_METADATA, meta)
        ObjectReader reader = mapper.reader(MoleculeObject.class).with(attrs2)
        Iterator result = reader.readValues(json)
        def mols2 = result.collect {
            it
        }
            
        then:
        json != null
        meta.getPropertyTypes().size() == 2
        
        mols2.size() == 2
        mols2[0].getValue("integer") instanceof Integer
        mols2[0].getValue("integer") == 0
        mols2[0].getValue("biginteger") instanceof BigInteger
        mols2[0].getValue("biginteger") == 0
        
        
    }
	
}

