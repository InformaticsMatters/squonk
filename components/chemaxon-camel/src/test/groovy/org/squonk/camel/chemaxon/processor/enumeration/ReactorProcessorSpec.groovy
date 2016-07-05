package org.squonk.camel.chemaxon.processor.enumeration

import chemaxon.formats.MolImporter
import chemaxon.struc.Molecule
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.apache.camel.builder.RouteBuilder
import org.squonk.chemaxon.molecule.MoleculeUtils
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject

/**
 *
 * @author timbo
 */
class ReactorProcessorSpec extends CamelSpecificationBase {


    void "react using files"() {
        setup:
        List mols = Molecules.nci100Molecules()
        mols.eachWithIndex { mo, c ->
            mo.putValue("R1_REACTANT", "R1_" + (c+1))
            mo.putValue("R2_REACTANT", "R2_" + (c+1))
        }
        Dataset dataset = new Dataset(MoleculeObject.class, mols)

        println "Read: ${mols.size()} reactants"
        def headers = [(ReactorProcessor.OPTION_REACTOR_REACTION):"amine-acylation"]

        def resultEndpoint = camelContext.getEndpoint('mock:result')
        resultEndpoint.expectedMessageCount(1)
        
        when:
        long t0 = System.currentTimeMillis()
        template.sendBodyAndHeaders('direct:start', dataset.getInputStream(false), headers)
        
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

