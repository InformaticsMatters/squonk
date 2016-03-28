package org.squonk.camel.chemaxon.processor

import com.im.lac.types.MoleculeObject
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import java.util.stream.*
import org.squonk.chemaxon.molecule.MoleculeObjectUtils
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class ChemAxonMoleculeProcessorSpec extends CamelSpecificationBase {
    
    //    String file = "../../data/testfiles/Building_blocks_GBP.sdf.gz"
    //    int count = 7003
    
    //    String file = "../../data/testfiles/nci100.smiles"
    //    int count = 100
    
//    String file = "../../data/testfiles/dhfr_standardized.sdf.gz"
//    int count = 756

     String file = "../../data/testfiles/Kinase_inhibs.sdf.gz"
    int count = 36
    
    long sleep = 0

    
    void "propcalc sequential streaming"() {
        setup:
        Thread.sleep(sleep)
        println "propcalc sequential streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(input).getStream(false);
        
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.prepareRequestBody('direct:streaming', mols, Dataset.class).getStream()
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        println "  ...done"
        Thread.sleep(sleep)
        
        then:
        println "  number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        
        cleanup: 
        input.close()
        
    }
    
    void "propcalc parallel streaming"() {
        setup:
        Thread.sleep(sleep)
        println "propcalc parallel streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(input).getStream(true);
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.prepareRequestBody('direct:streaming', mols, Dataset.class).getStream()
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        println "  ...done"
        Thread.sleep(sleep)
        
        then:
        println "  number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        cleanup: 
        input.close()
    }
    
    void "noop parallel streaming"() {
        setup:
        Thread.sleep(sleep)
        println "noop parallel streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(input).getStream(true);
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.prepareRequestBody('direct:noop', mols, Dataset.class).getStream()
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        println "  ...done"
        Thread.sleep(sleep)
        
        then:
        println "  number of mols: ${all.size()} generated in ${t1-t0}ms"
        all.size() == count
        
        cleanup: 
        input.close()
    }
    
    
    void "dataset streaming"() {
        setup:
        Thread.sleep(sleep)
        println "dataset streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(input).getStream(true);
        MoleculeObjectDataset mods = new MoleculeObjectDataset(mols)
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.prepareRequestBody('direct:noop', mods, Dataset.class).getStream()
        List all = Collections.unmodifiableList(results.collect(Collectors.toList()))
        long t1 = System.currentTimeMillis()
        println "  ...done"
        Thread.sleep(sleep)
        
        then:
        println "  number of mols: ${all.size()} generated in ${t1-t0}ms"
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
                    //.calculate("logd", "logD('7.4')")
                    .calculate("rings", "ringCount()")
                    .calculate("rot_bonds", "rotatableBondCount()")
                    //.transform("leconformer()")
                )       
                
                from("direct:noop")
                .process(new ChemAxonMoleculeProcessor()
                    .calculate("atoms", "atomCount()")
                )
            }
        }
    }
}
