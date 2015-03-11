package com.im.lac.camel.chemaxon.processor.enumeration

import com.im.lac.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

/**
 *
 * @author timbo
 */
class ReactorProcessorSpec extends CamelSpecificationBase {
	
    
    void "react using files"() {
        setup:
        String file1 = new File("../../data/testfiles/nci100.smiles").getCanonicalFile().toURI().toURL()
        String file2 = new File("../../data/testfiles/nci100.smiles").getCanonicalFile().toURI().toURL()
        String reaction = new File("../../data/testfiles/amine-acylation.mrv").getCanonicalFile().toURI().toURL()
        println "file1: $file1"
        println "file2: $file2"
        println "reaction: $reaction"
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        long t0 = System.currentTimeMillis()
        template.sendBodyAndHeaders('direct:start', null, 
            [Reaction:reaction, Reactants1: file1, Reactants2: file2])
        
        then:
        resultEndpoint.assertIsSatisfied()
        def stream = resultEndpoint.receivedExchanges.in.body[0]
        println "Stream: " + stream
        def results = stream.collect()
        long t1 = System.currentTimeMillis()
        println "Number of products: ${results.size()} generated in ${t1-t0}ms"
        results.size() > 0
    }
    
    
    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                .process(new ReactorProcessor())
                .to('mock:result')
            }
        }
    }
}

