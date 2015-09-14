package com.squonk.types.io

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.cfg.ContextAttributes
import com.fasterxml.jackson.databind.module.SimpleModule
import com.im.lac.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeObjectJsonDeserializerSpec extends Specification {
    
    static String jsonObject = '''{"source": "c1ccc2sc(SSc3nc4ccccc4s3)nc2c1", "values": {"mamma": 1234, "banana": "yellow"}, "format": "smiles"}'''

    static String jsonArray = '''[
        |{"source": "c1ccc2sc(SSc3nc4ccccc4s3)nc2c1", "values": {"mamma": 999}, "format": "smiles"}
        |,{"source": "O=[N+]([O-])c1cc(Cl)c(O)c([N+](=O)[O-])c1", "values": {}, "format": "smiles"}
        |,{"source": "N=c1[nH]cc([N+](=O)[O-])s1", "values": {}, "format": "smiles"}
        |,{"source": "Nc1ccc2c(c1)C(=O)c1ccccc1C2=O", "values": {}, "format": "smiles"}
        |,{"source": "O=C(O)c1ccccc1-c1c2ccc(O)c(Br)c2oc2c(Br)c(=O)ccc1-2", "values": {}, "format": "smiles"}
        |,{"source": "CN(C)C1=C(Cl)C(=O)c2ccccc2C1=O", "values": {"mamma": 1234, "banana": "green"}, "format": "smiles"}
        |]'''.stripMargin()

    static Map mappings = ["mamma": BigInteger.class, "banana": String.class]
    
    
    void "deserialize single"() {
        
        setup:
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer())
        mapper.registerModule(module)
        
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS,mappings)
        ObjectReader reader = mapper.reader(MoleculeObject.class).with(attrs)
        
        when:
        MoleculeObject mol = reader.readValue(jsonObject)
        
        then:
        //System.out.println("MO: " + mol);
        mol.getValue('banana') == 'yellow'
        mol.getValue('mamma').class == BigInteger.class
        
    }
    
    
    void "deserialize array"() {
        
        setup:
        ObjectMapper mapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer())
        mapper.registerModule(module)
        
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_VALUE_MAPPINGS, mappings)
        ObjectReader reader = mapper.reader(MoleculeObject.class).with(attrs)
        
        when:
        Iterator<MoleculeObject> mols = reader.readValues(jsonArray)
        
        then:
        mols.collect().size() == 6
    }
	
}

