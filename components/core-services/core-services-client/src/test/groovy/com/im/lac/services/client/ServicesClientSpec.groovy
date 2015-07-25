package com.im.lac.services.client

import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ServicesClientSpec extends Specification {   
    
    @Ignore
    void "list services"() {
        
        setup:
        println "list services()"
        def client = new ServicesClient()
        
        when:
        def definitions = client.getServiceDefinitions()
        println "received definitions " + definitions
        
        then: 
        definitions != null
        definitions.size() > 0
    }
     
}

