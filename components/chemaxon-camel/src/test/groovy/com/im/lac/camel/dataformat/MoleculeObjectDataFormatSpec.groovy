package com.im.lac.camel.dataformat

import org.apache.camel.spi.DataFormat
import spock.lang.Specification
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils
import com.im.lac.types.MoleculeObjectIterable

/**
 *
 * @author timbo
 */
class MoleculeObjectDataFormatSpec extends Specification {
    
    def 'read molecules'() {

        setup:
        File file = new File("../../data/testfiles/nci100.smiles")
        MoleculeObjectIterable mols = MoleculeObjectUtils.createIterable(file)
        MoleculeObjectDataFormat modf = new MoleculeObjectDataFormat()
        OutputStream out = new ByteArrayOutputStream()
        
        when:
        modf.marshal(mols, out)
        InputStream is = new ByteArrayInputStream(out.toByteArray())
        def moiter = modf.unmarshal(is)
        def mols2 = moiter.collect()
        
        then:
        mols2.size() == 100
    }
    
	
    def 'serialization speed and size'() {

        setup:
        File file = new File("../../data/testfiles/nci1000.smiles")
        MoleculeObjectIterable mols = MoleculeObjectUtils.createIterable(file)
        MoleculeObjectDataFormat modf = new MoleculeObjectDataFormat()
        File serFile = new File("../../data/testfiles/nci1000.ser")
        OutputStream out = new FileOutputStream(serFile)
        
        when:
        long t0 = System.currentTimeMillis() 
        modf.marshal(mols, out)
        out.close()
        long t1 = System.currentTimeMillis() 
        InputStream is = new FileInputStream(serFile)
        long t2 = System.currentTimeMillis() 
        def moiter = modf.unmarshal(is)
        def mols2 = moiter.collect()
        is.close()
        long t3 = System.currentTimeMillis()
        println "Serializing took ${t1-t0}ms, Deserializing took ${t3-t2}ms. File size is ${serFile.size()}"
        
        then:
        mols2.size() == 1000
        
        cleanup:
        serFile.delete()
    }
    
    
}

