package com.im.lac.camel.chemaxon.processor.clustering

import spock.lang.Specification
import com.chemaxon.descriptors.common.BinaryVectorDescriptor
import com.chemaxon.descriptors.fingerprints.ecfp.*
import com.chemaxon.descriptors.metrics.BinaryMetrics
import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.chemaxon.screening.MoleculeScreener
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils
import com.im.lac.types.MoleculeObject;
import org.apache.camel.builder.RouteBuilder
import java.util.stream.Stream

/**
 * Created by timbo on 14/04/2014.
 */
class SphereExclusionClusteringProcessorSpec extends CamelSpecificationBase {

    
    def "cluster stream"() {
        given:
        def input = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamProvider(input).getStream(false);
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
            
        when:
        template.sendBody('direct:simple', mols)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].collect()
        result.size() == 756
        int max = 0
        result.each {
            Integer cluster = it.getValue('cluster')
            assert cluster != null
            if (cluster > max) { max = cluster }
        }
        max > 0
        
        cleanup:
        input.close()

    }
    
    def "renamed"() {
        given:
        List<MoleculeObject> mols = [
            new MoleculeObject("CC1=CC(=O)C=CC1=O"),
            new MoleculeObject("S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4"),
            new MoleculeObject("OC1=C(Cl)C=C(C=C1[N+]([O-])=O)[N+]([O-])=O"),
            new MoleculeObject("[O-][N+](=O)C1=CNC(=N)S1"),
            new MoleculeObject("NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O"),
            new MoleculeObject("OC(=O)C1=C(C=CC=C1)C2=C3C=CC(=O)C(=C3OC4=C2C=CC(=C4Br)O)Br"),
            new MoleculeObject("CN(C)C1=C(Cl)C(=O)C2=C(C=CC=C2)C1=O"),
            new MoleculeObject("CC1=C(C2=C(C=C1)C(=O)C3=CC=CC=C3C2=O)[N+]([O-])=O"),
            new MoleculeObject("CC(=NO)C(C)=NO"),
            new MoleculeObject("C1=CC=C(C=C1)P(C2=CC=CC=C2)C3=CC=CC=C3")
        ]

        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
            
        when:
        template.sendBody('direct:renamed', mols)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].collect()
        result.size() == 10
        result.each {
            Integer cluster = it.getValue('abcd')
            assert cluster != null
        }
    }

    @Override
    RouteBuilder createRouteBuilder() {
              
        return new RouteBuilder() {
            
            EcfpGenerator gen = new EcfpParameters().getDescriptorGenerator(); // default ECFP
            
            public void configure() {
                from("direct:simple")
                .process(new SphereExclusionClusteringProcessor(
                        gen, gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO)))
                .to('mock:result')
                
                from("direct:renamed")
                .process(new SphereExclusionClusteringProcessor(
                        gen, gen.getBinaryMetricsComparator(BinaryMetrics.BINARY_TANIMOTO))
                    .clusterPropertyName('abcd'))
                .to('mock:result')
            }
        }
    }
}
