package com.im.lac.camel.chemaxon.processor

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.im.lac.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class StandardizerProcessorSpec extends CamelSpecificationBase {


    def resultEndpoint

    def 'standardizer processor'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mols = []
        mols << MolImporter.importMol('c1ccccc1')
        template.sendBody('direct:start', mols)

        then:
        resultEndpoint.assertIsSatisfied()
        List result = resultEndpoint.receivedExchanges.in.body[0]
        result.size() == 1
        result[0].atomCount == 12
        
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                .process(new StandardizerProcessor('addexplicith'))
                .to('mock:result')
            }
        }
    }
}
