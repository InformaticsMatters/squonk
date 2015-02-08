package examples

import com.im.lac.camel.testsupport.CamelSpecificationBase
import java.util.zip.GZIPInputStream
import org.apache.camel.builder.RouteBuilder
import spock.lang.FailsWith
import spock.lang.Shared

class PlatformNeutralMoleculesSpec extends CamelSpecificationBase {
    
    
    def 'smiles to molecules'() {
        
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        File file = new File("../../data/testfiles/nci1000.smiles")
        
        when:
        template.sendBody('direct:handleMoleculeObjects', file)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 1000
    }

    def 'InputStream to molecules'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))

        when:
        template.sendBody('direct:handleMoleculeObjects', gzip)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 756 // should be 756

        cleanup:
        gzip.close()
    }
    
   @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:handleMoleculeObjects")
                .log('Handling ${body}')
                .to("language:python:file:src/main/python/molecule_objects.py?transform=false")
                .to('mock:result')
                
            }
        }
    }
}

