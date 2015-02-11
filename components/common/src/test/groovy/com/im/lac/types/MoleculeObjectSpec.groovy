package com.im.lac.types

import spock.lang.Specification

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
        m2.getSourceAsString() == molStr
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
        mols2[0].getSourceAsString() == molStr1
        mols2[0].format == MoleculeObject.FORMAT_SMILES
        
        mols2[1].hasValue("crazy")
        mols2[1].getSourceAsString() == molStr2
        mols2[1].format == MoleculeObject.FORMAT_SMILES
            
    }
	
}

