package examples

import com.im.lac.camel.testsupport.CamelSpecificationBase
import java.util.zip.GZIPInputStream
import org.apache.camel.builder.RouteBuilder
import spock.lang.FailsWith
import spock.lang.Shared

class PythonExamplesSpec extends CamelSpecificationBase {
    
    @Shared smiles10 = '''\
CC1=CC(=O)C=CC1=O
S(SC1=NC2=CC=CC=C2S1)C3=NC4=C(S3)C=CC=C4
OC1=C(Cl)C=C(C=C1[N+]([O-])=O)[N+]([O-])=O
[O-][N+](=O)C1=CNC(=N)S1
NC1=CC2=C(C=C1)C(=O)C3=C(C=CC=C3)C2=O
OC(=O)C1=C(C=CC=C1)C2=C3C=CC(=O)C(=C3OC4=C2C=CC(=C4Br)O)Br
CN(C)C1=C(Cl)C(=O)C2=C(C=CC=C2)C1=O
CC1=C(C2=C(C=C1)C(=O)C3=CC=CC=C3C2=O)[N+]([O-])=O
CC(=NO)C(C)=NO
C1=CC=C(C=C1)P(C2=CC=CC=C2)C3=CC=CC=C3'''
    

    def 'smiles to molecules'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        template.sendBody('direct:convertToMols', smiles10)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 10 // should be 10
    }
    
    def 'smiles file to molecules'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        File file = new File("../../data/testfiles/nci1000.smiles")
        
        when:
        template.sendBody('direct:convertToMols', file)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 1000 // should be 1000
    }

    def 'InputStream to molecules'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))
        
        when:
        template.sendBody('direct:convertToMols', gzip)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 756 // should be 756
        
        cleanup:
        gzip.close()
    }


//    def 'InputStream to molecules to props'() {
//        setup:
//        def resultEndpoint = camelContext.getEndpoint('mock:result')
//        resultEndpoint.expectedMessageCount(1)
//        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))
//
//        when:
//        template.sendBody('direct:convertToMolsGetProps', gzip)
//
//        then:
//        resultEndpoint.assertIsSatisfied()
//        def result = resultEndpoint.receivedExchanges.in.body[0]
//        result == 756 // should be 756
//
//        cleanup:
//        gzip.close()
//    }


    def 'InputStream to molecule filter'() {
        setup:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))

        when:
        template.sendBody('direct:convertToMolsFilter', gzip)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0]
        result == 508 // was756

        cleanup:
        gzip.close()
    }



  @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:count")
                .to("language:python:classpath:counter.py?transform=false")
                .to('mock:result')
                
                from("direct:convertToMols")
                .to("language:python:classpath:convert_to_molecules.py?transform=false")
                .to("language:python:classpath:counter.py?transform=false")
                .to('mock:result')
// Third camel route to test property calculation
                from("direct:convertToMolsGetProps")
                .to("language:python:classpath:convert_to_molecules.py?transform=false")
                .setHeader('FUNCTION', constant("num_hba"))
                .to("language:python:classpath:calc_props.py?transform=false")
                .to('mock:result')

                from("direct:convertToMolsFilter")
                .to("language:python:classpath:convert_to_molecules.py?transform=false")
                .setHeader('FUNCTION', constant("2<num_hba<7"))
                .to("language:python:classpath:filter_props.py?transform=false")
                .to('mock:result')



            }
        }
    }
}

