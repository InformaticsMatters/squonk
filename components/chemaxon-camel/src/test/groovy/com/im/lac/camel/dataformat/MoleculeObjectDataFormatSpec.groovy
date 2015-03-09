package com.im.lac.camel.dataformat

import org.apache.camel.spi.DataFormat
import spock.lang.Specification
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils
import com.im.lac.types.MoleculeObject
import java.util.stream.Stream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import com.im.lac.camel.dataformat.StreamingIteratorJsonDataFormat

/**
 *
 * @author timbo
 */
class MoleculeObjectDataFormatSpec extends Specification {
    
    //    String file = "../../data/testfiles/nci1000.smiles"
    //    int count = 1000
    String file = "../../data/testfiles/dhfr_standardized.sdf.gz"
    int count = 756
    //String file = "../../data/testfiles/Building_blocks_GBP.sdf.gz"
    //int count = 7003 

    
//    def 'read molecules'() {
//
//        setup:
//        File file = new File("../../data/testfiles/nci100.smiles")
//        MoleculeObjectIterable mols = MoleculeObjectUtils.createIterable(file)
//        MoleculeObjectDataFormatXXX modf = new MoleculeObjectDataFormatXXX()
//        OutputStream out = new ByteArrayOutputStream()
//        
//        when:
//        modf.marshal(mols, out)
//        InputStream is = new ByteArrayInputStream(out.toByteArray())
//        def moiter = modf.unmarshal(is)
//        def mols2 = moiter.collect()
//        
//        then:
//        mols2.size() == 100
//    }
    
	
//    def 'serialization speed and size'() {
//
//        setup:
//        File file = new File(file)
//        MoleculeObjectIterable mols = MoleculeObjectUtils.createIterable(file)
//        List<MoleculeObject> molsList = mols.collect()
//        MoleculeObjectDataFormatXXX modf = new MoleculeObjectDataFormatXXX()
//        
//        ByteArrayOutputStream out = new ByteArrayOutputStream()
//        
//        when:
//        long t0 = System.currentTimeMillis() 
//        modf.marshal(molsList, out)
//        long t1 = System.currentTimeMillis()
//        byte[] bytes =  out.toByteArray()
//        def size = bytes.length
//        
//        ByteArrayInputStream is = new ByteArrayInputStream(bytes)
//        long t2 = System.currentTimeMillis() 
//        def moiter = modf.unmarshal(is)
//        def mols2 = moiter.collect()
//        is.close()
//        long t3 = System.currentTimeMillis()
//        println "Serializing took ${t1-t0}ms, Deserializing took ${t3-t2}ms. Serialized size is $size bytes"
//        
//        then:
//        mols2.size() == count     
//    }
    
    def 'marshaling/unmarshaling speed and size'() {

        setup:
        println 'marshaling/unmarshaling speed and size'
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStream(new FileInputStream(file))
        println "mols: " + mols
        def dataFormat = new StreamingIteratorJsonDataFormat<MoleculeObject>(MoleculeObject.class)
        
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        
        when:
        long t0 = System.currentTimeMillis() 
        dataFormat.marshal(null, mols, out)
        long t1 = System.currentTimeMillis()
        byte[] bytes =  out.toByteArray()
        def size = bytes.length
        
        ByteArrayInputStream is = new ByteArrayInputStream(bytes)
        long t2 = System.currentTimeMillis() 
        def moiter = dataFormat.unmarshal(null, is)
        def mols2 = moiter.collect()
        is.close()
        long t3 = System.currentTimeMillis()
        println "Writing JSON took ${t1-t0}ms, Reading JSON took ${t3-t2}ms. Size is $size bytes"
        
        then:
        mols2.size() == count
        
        cleanup:
        mols.close()
        
    }
    
    def 'gzipped marshaling/unmarshaling speed and size'() {

        setup:
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStream(new FileInputStream(file))
        def dataFormat = new StreamingIteratorJsonDataFormat<MoleculeObject>(MoleculeObject.class)
        
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        GZIPOutputStream gzip = new GZIPOutputStream(out)
        
        when:
        long t0 = System.currentTimeMillis() 
        dataFormat.marshal(null, mols, gzip)
        long t1 = System.currentTimeMillis()
        gzip.flush()
        gzip.finish()
        byte[] bytes =  out.toByteArray()

        def size = bytes.length
        
        ByteArrayInputStream is = new ByteArrayInputStream(bytes)
        GZIPInputStream gunzip = new GZIPInputStream(is)
        long t2 = System.currentTimeMillis() 
        def moiter = dataFormat.unmarshal(null, gunzip)
        def mols2 = moiter.collect()

        long t3 = System.currentTimeMillis()
        println "Writing Gzipped JSON took ${t1-t0}ms, Reading Gzipped JSON took ${t3-t2}ms. Size is $size bytes"
        
        then:
        mols2.size() == count
        
        cleanup:
        mols.close()
        
    }
    
}

