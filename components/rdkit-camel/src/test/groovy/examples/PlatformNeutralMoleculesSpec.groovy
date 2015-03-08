package example

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

    def 'smiles to molecules lipinski'() {

        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        File file = new File("../../data/testfiles/nci1000.smiles")

        when:
        template.sendBody('direct:convertToMolsFilter', file)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 93
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

    }

    def 'InputStream to threaded molecule filter'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("/root/lac/components/Kinase_inhibs.sdf.gz"))

        when:
        template.sendBody('direct:handleMoleculeObjects', gzip)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 15 // FOR NOW -> SHOULD 508 // was756

        cleanup:
        gzip.close()
    }

    
   @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:handleMoleculeObjects")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .setHeader('FUNCTION', constant("num_hba"))
                .to("language:python:classpath:calc_props_thread.py?transform=false")
                .to("language:python:classpath:molecule_counter.py?transform=false")
                .to('mock:result')


                from("direct:convertToMolsFilter")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .setHeader('FUNCTION', constant("-1<num_hbd<6"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .setHeader('FUNCTION', constant("-1<num_hba<11"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .setHeader('FUNCTION', constant("5<mol_logp<100"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")                
                .setHeader('FUNCTION', constant("0<mol_mr<500"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .to("language:python:classpath:molecule_counter.py?transform=false")
                .to('mock:result')
                
            }
        }
    }
}

