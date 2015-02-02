package com.im.lac.types

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeObjectSpec extends Specification {
    
    def "test serialization"() {
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
        m2.getSourceAsString() == molStr
        m2.hasValue("foo")
        m2.format == MoleculeObject.FORMAT_SMILES
        !m2.hasRepresentation("donald")
            
    }
	
}

