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
class ChemTermsProcessorDynamicSpec extends CamelSpecificationBase {

    
    def 'ChemTerms processor for Molecule'() {

        given:
        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        

        when:
        def mol0 = MolImporter.importMol('C') 
        template.sendBodyAndHeader('direct:chemTermsCalculator', mol0, 
            ChemTermsProcessor.PROP_EVALUATORS_DEFINTION, ['atom_Count':'atomCount()','bond_count':'bondCount()'])
        

        then:
        resultEndpoint.assertIsSatisfied()
        Molecule result0 = resultEndpoint.receivedExchanges.in.body[0]
        result0.getPropertyObject('atom_count') == 5
        result0.getPropertyObject('bond_count') == 4

        
    }

    @Override
    RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:chemTermsCalculator")
                .process(new ChemTermsProcessor())
                .to('mock:result')
            }
        }
    }
}
