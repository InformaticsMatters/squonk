package example

import com.im.lac.camel.testsupport.CamelSpecificationBase
import java.util.zip.GZIPInputStream
import org.apache.camel.builder.RouteBuilder
import spock.lang.FailsWith
import spock.lang.Shared

class RDKitExtraTest extends CamelSpecificationBase {
    
    
    def 'sdf stream to mcs'() {
        
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))
        
        when:
        template.sendBody('direct:findMCS', gzip)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == "[#7](:,-[#6]-,:[#7]):,-[#6](:,-[#6])-,:[#7]"
    }
    def 'sdf stream to clusters'() {

        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))

        when:
        template.sendBody('direct:findclusts', gzip)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        // For now we just find the size of the dist matrix because clustering doesn't work
        result == 286146
    }

    def 'sdf stream to mmps'() {

        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))

        when:
        template.sendBody('direct:findmmps', gzip)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        // For now we just find the size of the dist matrix because clustering doesn't work
        result == 130
    }
    
   @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:findMCS")
                .to("language:python:file:src/main/python/molecule_objects.py?transform=false")
                .to("language:python:file:src/main/python/find_mcs.py?transform=false")
                .to('mock:result')

                 from("direct:findclusts")
                .to("language:python:file:src/main/python/molecule_objects.py?transform=false")
                .setHeader('FINGERPRINT', constant("morgan"))
                .setHeader('CLUSTERING', constant("butina"))
                .setHeader('SIMILARITY', constant("dice"))
                .to("language:python:file:src/main/python/cluster_mols.py?transform=false")
                .to('mock:result')

                 from("direct:findmmps")
                .to("language:python:file:src/main/python/molecule_objects.py?transform=false")
                .to("language:python:file:src/main/python/find_mmps.py?transform=false")
                .to('mock:result')               
               
            }
        }
    }
}

