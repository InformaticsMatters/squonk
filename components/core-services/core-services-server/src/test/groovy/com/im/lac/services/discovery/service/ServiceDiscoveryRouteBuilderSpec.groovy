package com.im.lac.services.discovery.service

import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ServiceDiscoveryRouteBuilderSpec extends Specification {
	
    void "test service discovery"() {
        setup:        
        DefaultCamelContext context = new DefaultCamelContext()
        ServiceDiscoveryRouteBuilder rb = new ServiceDiscoveryRouteBuilder()
        rb.timerRepeats = 1
        context.addRoutes(rb)
        context.start()

        
        when:
        sleep(5000)
        
        then:
        println "Discovered ${rb.serviceDefinitions.size()} active service definitions"
        rb.serviceDefinitions.size() > 0
                
        cleanup:
        context.stop()
        
        
    }
    
}

