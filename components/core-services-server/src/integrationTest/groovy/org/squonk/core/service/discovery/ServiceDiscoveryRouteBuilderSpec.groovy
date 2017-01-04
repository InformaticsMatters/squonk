package org.squonk.core.service.discovery

import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.squonk.core.CommonConstants
import org.squonk.execution.docker.DescriptorRegistry
import spock.lang.Specification

import static org.squonk.core.service.discovery.ServiceDiscoveryRouteBuilder.TEST_SERVICE_DESCRIPTORS

/**
 *
 * @author timbo
 */
class ServiceDiscoveryRouteBuilderSpec extends Specification {
	
    void "test service discovery"() {
        setup:

        SimpleRegistry reg = new SimpleRegistry()
        reg.put(CommonConstants.KEY_DOCKER_SERVICE_REGISTRY, new DescriptorRegistry())
        DefaultCamelContext context = new DefaultCamelContext(reg)
        ServiceDiscoveryRouteBuilder rb = new ServiceDiscoveryRouteBuilder()
        rb.timerRepeats = 1
        context.addRoutes(rb)
        context.start()
        ProducerTemplate pt = context.createProducerTemplate()
        // push some dummy service descriptors. There is only one.
        rb.updateServiceDescriptors("ignored", null, Arrays.asList(TEST_SERVICE_DESCRIPTORS))

        
        when:
        sleep(4000)
        def results = pt.requestBody(ServiceDiscoveryRouteBuilder.ROUTE_REQUEST, null)
        
        then:
        println "Discovered ${results.size()} active service definitions"
        results.size() > 1 // confirm we get at least that one service descriptor back
        boolean b = false
        results.each {
            println "  SD ${it.id}"
            if (it.id == "test.noop") {
                b = true
            }
        }
        b == true
                
        cleanup:
        context.stop()
    }
    
}

