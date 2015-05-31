package com.im.lac.camel.chemaxon.processor.screening

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.chemaxon.descriptors.fingerprints.ecfp.*
import com.chemaxon.descriptors.fingerprints.pf2d.PfGenerator
import com.chemaxon.descriptors.fingerprints.pf2d.PfParameters
import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.chemaxon.screening.MoleculeScreener
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.types.MoleculeObject
import com.im.lac.stream.FixedBatchSpliterator
import org.apache.camel.builder.RouteBuilder
import java.util.stream.*

/**
 * Created by timbo on 14/04/2014.
 */
class MoleculeScreenerProcessorSpec extends CamelSpecificationBase {

    String file = "../../data/testfiles/dhfr_standardized.sdf.gz"
    int resultCount = 4
    //String file = "../../data/testfiles/Building_blocks_GBP.sdf.gz"
    //int resultCount = 235
    
    String target = "CCN(C)C1=C(Br)C(=O)C2=C(C=CC=C2)C1=O"


    def resultEndpoint
    
    def 'screen for single fixed'() {

        setup:
        def mol = new MoleculeObject("CN(C)C1=C(Cl)C(=O)C2=C(C=CC=C2)C1=O")

        when:
        MoleculeObject result = template.requestBody('direct:pharmacophore/streaming', mol)

        then:
        double similarity = result.getValue('similarity')
        similarity > 0 && similarity < 1
    }
    
    def 'dynamic target'() {

        setup:
        def mol1 = new MoleculeObject("NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O")
        def mol2 = new MoleculeObject(target)
        
        when:
        MoleculeObject result = template.requestBodyAndHeader('direct:pharmacophore/streaming', mol1, MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE, mol2)

        then:
        Double similarity = result.getValue('similarity')
        similarity > 0 && similarity < 1 // if header not read then would be 1.0
    }
    
    
    void "pharmacophore sequential streaming"() {
        setup:
        println "pharmacophore sequential streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(input).getStream(false);
        def mol2 = new MoleculeObject(target)
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBodyAndHeader('direct:pharmacophore/streaming', mols, MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE, mol2).getStream()
        long count = results.count()
        long t1 = System.currentTimeMillis()
        println "...done"
        
        then:
        println "Number of mols: $count generated in ${t1-t0}ms"
        count == resultCount
        
        cleanup: 
        input.close()
    }
    
    void "pharmacophore parallel streaming"() {
        setup:
        println "pharmacophore parallel streaming"
        InputStream input = new FileInputStream(file)
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(input).getStream(true);
        def mol2 = new MoleculeObject(target)
        
        when:
        long t0 = System.currentTimeMillis()
        Stream results = template.requestBodyAndHeader('direct:pharmacophore/streaming', mols, MoleculeScreenerProcessor.HEADER_QUERY_MOLECULE, mol2)getStream()
        long count = results.count()
        long t1 = System.currentTimeMillis()
        println "...done"
        
        then:

        println "Number of mols: $count generated in ${t1-t0}ms"
        count == resultCount
        
        cleanup:
        input.close()
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
