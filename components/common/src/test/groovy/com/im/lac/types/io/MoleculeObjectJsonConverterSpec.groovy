package com.im.lac.types.io

import com.fasterxml.jackson.databind.ObjectMapper
import com.im.lac.types.MoleculeObject
import spock.lang.Specification
import java.util.stream.Collectors

/**
 *
 * @author timbo
 */
class MoleculeObjectJsonConverterSpec extends Specification {
    
    
    
    void "marshal moleculeobjects"() {
        
        setup:
        def input = [new MoleculeObject('CCC'),new MoleculeObject('c1ccccc')]
            
        ObjectMapper mapper = new ObjectMapper()
        
        when:
        String json = mapper.writeValueAsString(input[0])
        println "JSON: " + json
        
        then:
        json.indexOf('representations') == -1
    }
    
    void "ObjectMapper for list of MoleculeObjects with props"() {
        
        setup:
        def input = [
            new MoleculeObject('CCC', 'smiles', [prop1: 'hello', prop2: 'banana', prop3: 999]),
            new MoleculeObject('c1ccccc', 'smiles', [prop1: 'goodbye', prop2: 'orange', prop3: 666])]
            
        ObjectMapper mapper = new ObjectMapper()
        
        when:
        String json = mapper.writeValueAsString(input)
        println "JSON: " + json
        
        then:
        json.indexOf('banana') > 0
    }
    
    void "marshal MoleculeObjects with props"() {
        
        setup:
        def input = [
            new MoleculeObject('CCC', 'smiles', [prop1: 'hello', prop2: 'banana', prop3: 999]),
            new MoleculeObject('c1ccccc', 'smiles', [prop1: 'goodbye', prop2: 'orange', prop3: 666])
        ]
            
        def convertor = new MoleculeObjectJsonConverter()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        
        when:
        def meta = convertor.marshal(input.stream(), out)
        println "META as String: $meta"
        ObjectMapper mapper = new ObjectMapper()
        def metaJson = mapper.writeValueAsString(meta)
        println "META as JSON: $metaJson"
        
        String json = out.toString()
        println "JSON: " + json
        
        then:
        meta != null
        meta.size == 2
        json.indexOf('banana') > 0
    }
    
    void "unmarshal moleculeobjects"() {
            
        setup:
        String input = '''[
    {"format":"smiles","source":"c1ccccc1","values":{"field_0":1}},
    {"format":"smiles","source":"CCC","values":{"field_0":2}}
    ]'''
        Metadata meta = new Metadata()
        meta.size = 2
        meta.propertyTypes.put("field_0", Integer.class)
        def convertor = new MoleculeObjectJsonConverter()
            
        when:
        def result = convertor.unmarshal(meta, new ByteArrayInputStream(input.getBytes()))
        def mols = result.collect()
            
        then:
        mols.size() == 2
        mols[0].source == 'c1ccccc1'
        mols[0].values["field_0"] == 1
    }
    
     void "marshal unmarshal MoleculeObjects with props"() {
        
        setup:
        def input = [
            new MoleculeObject('CCC', 'smiles', [prop1: 'hello', prop2: 'banana', prop3: new BigInteger(999)]),
            new MoleculeObject('c1ccccc', 'smiles', [prop1: 'goodbye', prop2: 'orange', prop3: new BigInteger(666)])]
            
        def convertor = new MoleculeObjectJsonConverter()
        
        when:
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        def meta = convertor.marshal(input.stream(), out)
        String json = out.toString()
        def results = convertor.unmarshal(meta, new ByteArrayInputStream(json.bytes)).collect(Collectors.toList())
        
        
        then:
        json.indexOf('banana') > 0
        results.size() == 2
        results[0].source == 'CCC'
        results[0].getValue('prop3') instanceof BigInteger
        results[0].getValue('prop3') == 999
        results[1].getValue('prop3') == 666
        
    }
    	
}

