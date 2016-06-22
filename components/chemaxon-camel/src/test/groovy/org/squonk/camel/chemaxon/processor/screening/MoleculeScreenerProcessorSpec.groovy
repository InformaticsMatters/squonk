package org.squonk.camel.chemaxon.processor.screening

import chemaxon.formats.MolImporter
import com.chemaxon.descriptors.fingerprints.pf2d.PfGenerator
import com.chemaxon.descriptors.fingerprints.pf2d.PfParameters
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.chemaxon.screening.MoleculeScreener
import org.squonk.types.MoleculeObject
import org.apache.camel.builder.RouteBuilder
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset

import java.util.stream.*

/**
 * Created by timbo on 14/04/2014.
 */
class MoleculeScreenerProcessorSpec extends CamelSpecificationBase {


    // bb
    //int resultCount = 235

    //dhrf
    //int resultCount = 4
    //String target = "CCN(C)C1=C(Br)C(=O)C2=C(C=CC=C2)C1=O"
    String target = 'OC1=CC(=CC=C1)C1=NC2=C(OC3=NC=CC=C23)C(=N1)N1CCOCC1'
    int resultCount = 8
    
    void "pharmacophore sequential streaming"() {
        setup:
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        dataset.replaceStream(dataset.getStream().sequential())
        def mol2 = new MoleculeObject(target)
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBodyAndHeaders(
                'direct:pharmacophore/streaming', dataset,
                [(MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE+"_source"): target,
                 (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE+"_format"): "smiles"]).getStream()
        long count = results.count()
        long t1 = System.currentTimeMillis()
        
        then:
        println "Number of mols: $count generated in ${t1-t0}ms"
        count == resultCount
        
        cleanup:
        results.close()
    }
    
    void "pharmacophore parallel streaming"() {
        setup:
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        dataset.replaceStream(dataset.getStream().parallel())
        def mol2 = new MoleculeObject(target)
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBodyAndHeaders(
                'direct:pharmacophore/streaming', dataset,
                [(MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE+"_source"): target,
                 (MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE+"_format"): "smiles"]).getStream()
        long count = results.count()
        long t1 = System.currentTimeMillis()
        println "...done"
        
        then:

        println "Number of mols: $count generated in ${t1-t0}ms"
        count == resultCount
        
        cleanup:
        results.close()
    }
    
    @Override
    RouteBuilder createRouteBuilder() {
        
        PfParameters pfParams = PfParameters.createNewBuilder().build();
        PfGenerator pfGenerator = pfParams.getDescriptorGenerator();
        MoleculeScreener pfScreener = new MoleculeScreener(pfGenerator, pfGenerator.getDefaultComparator());
        pfScreener.setTargetMol(MolImporter.importMol("NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O"))
        
        return new RouteBuilder() {
            public void configure() {
                               
                from("direct:pharmacophore/streaming")
                .process(new MoleculeScreenerProcessor(pfScreener))
                
            }
        }
    }
}
