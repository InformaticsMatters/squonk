package org.squonk.camel.testsupport

import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification
import org.apache.camel.*
import org.apache.camel.builder.*


/**
 * Created by timbo on 13/04/2014.
 */
abstract class CamelSpecificationBase extends Specification {

    def camelContext
    def template

    def setup() {
        camelContext = createCamelContext()
        RouteBuilder rb = createRouteBuilder()
        println "ctx=$camelContext rb=$rb"
        camelContext.addRoutes(rb)
        camelContext.start()
        template = camelContext.createProducerTemplate()
    }

    def cleanup() {
        template?.stop()
        camelContext?.stop()
    }

    CamelContext createCamelContext() {
        new DefaultCamelContext()
    }

    abstract RouteBuilder createRouteBuilder()


}
