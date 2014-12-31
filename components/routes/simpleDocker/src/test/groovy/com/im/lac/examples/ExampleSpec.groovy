package com.im.lac.examples

import spock.lang.Specification
import spock.lang.Shared
import groovy.json.JsonSlurper
import org.apache.camel.*
import org.apache.camel.impl.*


class ExampleSpec extends Specification {
    
    @Shared def camelContext
    @Shared String baseUrl
    
    def setupSpec() {
        println "starting context"
        
        def rb = new ExampleRouteBuilder()
        baseUrl = "http://" + rb.host + ":" + rb.port
        println "Base URL: $baseUrl"
        
        camelContext = new DefaultCamelContext()
        camelContext.addRoutes(rb)
        camelContext.start()
    }
    
    def cleanupSpec() {
        println "stopping context"
        camelContext.stop()
    }
    
    def "test direct"() {
        println "test direct"
        
        setup:
        def template = camelContext.createProducerTemplate()
        def msg = 'Hello World!'
        
        when:
        def result = template.requestBody('direct:tolowercase', msg)
        
        then:
        result == msg.toLowerCase()
    }
    
    def "test simple rest"() {
        println "test rest"
        
        setup:
        def template = camelContext.createProducerTemplate()
        def msg = 'Hello World!'
        
        when:
        def resp = template.requestBody("restlet:${baseUrl}/convert?restletMethod=POST", msg)
        
        then:
        resp == msg.toLowerCase()
    }
    
                
    def "test rest with json"() {
        println "test rest with json"
        
        setup:
        def template = camelContext.createProducerTemplate()
        def msg = [123.4, 532.2, 54.23, 86.34]
        
        when:
        def resp = template.requestBody("restlet:${baseUrl}/statistics?restletMethod=POST", msg)
        def slurper = new JsonSlurper()
        def result = slurper.parseText(resp)
 
        then:
        result.count == 4
    }
	
}

