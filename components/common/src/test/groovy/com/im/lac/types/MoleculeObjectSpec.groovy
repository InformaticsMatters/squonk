package com.im.lac.types

import spock.lang.Specification

import com.fasterxml.jackson.databind.ObjectMapper

/**
 *
 * @author timbo
 */
class MoleculeObjectSpec extends Specification {
    
    def "test serialization single"() {
        setup:
        String  molStr = "c1cccccc1"
        MoleculeObject m1 = new MoleculeObject(molStr, MoleculeObject.FORMAT_SMILES)
        m1.putValue("foo", "bar")
        m1.putRepresentation("donald", "duck")
        
        when:
        // serialize
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        ObjectOutputStream outStream = new ObjectOutputStream(bout)
        outStream.writeObject(m1)
        outStream.close()
        // deserialize
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray())
        ObjectInputStream inStream = new ObjectInputStream(bin)
        MoleculeObject m2 = inStream.readObject() 
        inStream.close()
        
        then:
        m1.hasValue("foo")
        m1.hasRepresentation("donald")
        m2.getSource() == molStr
        m2.hasValue("foo")
        m2.format == MoleculeObject.FORMAT_SMILES
        !m2.hasRepresentation("donald") 
    }
    
    def "test serialization list"() {
        setup:
        List mols1 = []
        String  molStr1 = "c1cccccc1"
        MoleculeObject m1 = new MoleculeObject(molStr1, MoleculeObject.FORMAT_SMILES)
        m1.putValue("foo", "bar")
        m1.putRepresentation("donald", "duck")
        mols1 << m1
        
        String  molStr2 = "c1cccccc1C"
        MoleculeObject m2 = new MoleculeObject(molStr2, MoleculeObject.FORMAT_SMILES)
        m2.putValue("crazy", "horse")
        mols1 << m2
        
        when:
        // serialize
        ByteArrayOutputStream bout = new ByteArrayOutputStream()
        ObjectOutputStream outStream = new ObjectOutputStream(bout)
        outStream.writeObject(mols1)
        outStream.close()
        // deserialize
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray())
        ObjectInputStream inStream = new ObjectInputStream(bin)
        List<MoleculeObject> mols2 = inStream.readObject() 
        inStream.close()
        
        then:
        mols2.size() == 2
        mols2[0].getValue("foo") == "bar"
        mols2[0].getSource() == molStr1
        mols2[0].format == MoleculeObject.FORMAT_SMILES
        
        mols2[1].hasValue("crazy")
        mols2[1].getSource() == molStr2
        mols2[1].format == MoleculeObject.FORMAT_SMILES
            
    }
    
    void "serialization speed"() {
        
    }
    
    
    void "marshal to json"() {
        
        setup:
        MoleculeObject mo = new MoleculeObject('CCC')
        mo.putValue('tim', 'tom')
            
        ObjectMapper mapper = new ObjectMapper()
        
        when:
        String json = mapper.writeValueAsString(mo)
        println "JSON: " + json
        
        then:
        json.indexOf('representations') == -1
        json.indexOf('values') > 0
    }
    
    void "unmarshal from json"() {
        
        setup:
        def json = '''{"source":"CCCCCC","values":{"name":"tom","age":99}}'''
            
        ObjectMapper mapper = new ObjectMapper()
        
        when:
        MoleculeObject mo = mapper.readValue(json, MoleculeObject.class)
        println "MO: " + mo
        
        then:
        mo.getSource() == "CCCCCC"
        mo.getValue('name') == 'tom'
        mo.getValue('age') == 99
    }
	
}

