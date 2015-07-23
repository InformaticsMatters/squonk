package com.im.lac.services.client

import com.im.lac.dataset.DataItem
import com.im.lac.job.client.JobClient
import com.im.lac.job.jobdef.*
import java.util.stream.Stream
import spock.lang.FailsWith
import spock.lang.Ignore
import java.util.stream.Collectors
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

