package com.im.lac.camel.chemaxon.processor

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.formats.MolExporter
import chemaxon.struc.Molecule
import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import org.apache.camel.builder.RouteBuilder

/**
 * Created by timbo on 14/04/2014.
 */
class StandardizerProcessorSpec extends CamelSpecificationBase {


    def resultEndpoint

    def 'standardizer for list'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mols = []
        mols << new MoleculeObject('c1ccccc1')
        template.sendBody('direct:start', mols)

        then:
        resultEndpoint.assertIsSatisfied()
        def result = resultEndpoint.receivedExchanges.in.body[0].getStream().collect()
        result.size() == 1
        result[0].getRepresentation(Molecule.class.getName()) != null
        result[0].getRepresentation(Molecule.class.getName()).atomCount == 12
        
    }
    
    def 'standardizer for single'() {

        given:
        resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mol = new MoleculeObject('c1ccccc1')
        template.sendBody('direct:start', mol)

        then:
        resultEndpoint.assertIsSatisfied()
        MoleculeObject result = resultEndpoint.receivedExchanges.in.body[0]
        result.getRepresentation(Molecule.class.getName()) != null
        result.getRepresentation(Molecule.class.getName()).atomCount == 12
        
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
