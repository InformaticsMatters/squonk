package org.squonk.core.client

import spock.lang.Ignore
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ServicesClientSpec extends Specification {   
    
    String username = 'squonkuser'
    
    @Ignore
    void "list services"() {
        
        setup:
        println "list services()"
        def client = new ServicesClient()
        
        when:
        def definitions = client.getServiceDefinitions(username)
        println "received definitions " + definitions
        
        then: 
        definitions != null
        definitions.size() > 0
    }
     
}

