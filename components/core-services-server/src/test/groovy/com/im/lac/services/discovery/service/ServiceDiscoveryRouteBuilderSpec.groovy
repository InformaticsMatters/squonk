package com.im.lac.services.discovery.service

import com.im.lac.services.ServerConstants
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import spock.lang.Ignore
import spock.lang.Specification

import static com.im.lac.services.discovery.service.ServiceDiscoveryRouteBuilder.TEST_SERVICE_DESCRIPTORS

/**
 *
 * @author timbo
 */
class ServiceDiscoveryRouteBuilderSpec extends Specification {
	
    void "test service discovery"() {
        setup:     
        SimpleRegistry registry = new SimpleRegistry()
        ServiceDescriptorStore serviceDescriptorStore = new ServiceDescriptorStore();
        serviceDescriptorStore.addServiceDescriptors("ignored", TEST_SERVICE_DESCRIPTORS);
        registry.put(ServerConstants.SERVICE_DESCRIPTOR_STORE, serviceDescriptorStore)
        DefaultCamelContext context = new DefaultCamelContext(registry)
        ServiceDiscoveryRouteBuilder rb = new ServiceDiscoveryRouteBuilder()
        rb.timerRepeats = 1
        context.addRoutes(rb)
        context.start()
        ProducerTemplate pt = context.createProducerTemplate()

        
        when:
        sleep(4000)
        def results = pt.requestBody(ServiceDiscoveryRouteBuilder.ROUTE_REQUEST, null)
        
        then:
        println "Discovered ${results.size()} active service definitions"
        results.size() > 0
        results.each {
            println "  SD ${it.id}"
        }
                
        cleanup:
        context.stop()
    }
    
}

