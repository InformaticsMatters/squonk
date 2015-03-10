package com.im.lac.camel.chemaxon.processor

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.im.lac.camel.chemaxon.processor.screening.MoleculeScreenerProcessor
import com.im.lac.camel.chemaxon.processor.screening.MoleculeScreenerProcessor
import com.im.lac.chemaxon.molecule.ChemTermsEvaluator
import com.im.lac.types.MoleculeObject
import java.util.stream.*
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils
import com.im.lac.chemaxon.screening.MoleculeScreener
import com.im.lac.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder
import com.im.lac.util.IOUtils

/**
 * Created by timbo on 14/04/2014.
 */
class ChemAxonMoleculeProcessorSpec extends CamelSpecificationBase {
    
//    String file = "../../data/testfiles/Building_blocks_GBP.sdf.gz"
//    int count = 7003
//    int filterCount = 235
    
    String file = "../../data/testfiles/nci100.smiles"
    int count = 100
    int filterCount = 2
    
    long sleep = 10


    
    void "propcalc sequential streaming"() {
        setup:
        Thread.sleep(sleep)
        println "propcalc sequential streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamProvider(input).getStream(false);
        
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBody('direct:streaming', mols)
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        println "...done"
        Thread.sleep(sleep)
        
        then:
        println "Number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        
        cleanup: 
        input.close()
        
    }
    
    void "propcalc parallel streaming"() {
        setup:
        Thread.sleep(sleep)
        println "propcalc parallel streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamProvider(input).getStream(true);
        
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBody('direct:streaming', mols)
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        println "...done"
        Thread.sleep(sleep)
        
        then:
        println "Number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        
        cleanup: 
        input.close()

    }
    
    
    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                
                
                from("direct:streaming")
                .process(new ChemAxonMoleculeProcessor()
                    //.standardize("removefragment:method=keeplargest..aromatize..removeexplicith")
                    .calculate("mol_weight", "mass()")
                    .calculate("logp", "logP()")
                    .calculate("hbd_count", "donorCount()")
                    .calculate("hba_count", "acceptorCount()")
                    .calculate("logd", "logD('7.4')")
                    .calculate("rings", "ringCount()")
                    .calculate("rot_bonds", "rotatableBondCount()")
                    //.transform("leconformer()")
                )               
            }
        }
    }
}
