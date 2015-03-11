package com.im.lac.demo.routes

import com.im.lac.camel.testsupport.CamelSpecificationBase
import com.im.lac.types.MoleculeObject
import java.util.stream.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.util.IOHelper


/**
 *
 * @author timbo
 */
class ReactorRoutesSpec extends CamelSpecificationBase {
    
    void "simple react"() {
        setup:
        InputStream is = new FileInputStream("../../data/testfiles/amine-acylation.mrv")
        String rxn = IOHelper.loadText(is);
        String url = new File("../../data/testfiles/nci1000.smiles").getCanonicalFile().toURI().toURL()

        
        when:
        long t0 = System.currentTimeMillis()
        def results = template.requestBodyAndHeaders("direct:reactor", rxn, [
                Reactants1: url,
                Reactants2: url
            ])
        long count = results.getStream().count()
        long t1 = System.currentTimeMillis()
        
        then:
        count > 0
        println "found $count products in ${t1-t0}ms"
        
        cleanup:
        is.close()
    }
    
    @Override
    RouteBuilder createRouteBuilder() {
        return new ReactorRouteBuilder()
    }
	
}

