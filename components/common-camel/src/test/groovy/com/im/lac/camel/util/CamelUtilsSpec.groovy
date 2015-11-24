package com.im.lac.camel.util

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CamelUtilsSpec extends Specification {
    
    void "generateUrlUsingQueryParams"() {

        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo", ["query.apple":"green","query.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green&banana=yellow"
        headers.size() == 0
        
    }
    
    void "generateUrlUsingHeaders"() {
        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo", ["header.apple":"green","header.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo"
        headers.size() == 2
    }
	
    
    void "generateUrlUsingHeadersAndQueryParams"() {
        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo", ["query.apple":"green","header.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green"
        headers.size() == 1
        
    }
    
    
    void "generateUrlSkippedParams"() {

        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo",
            ["query.apple":"green","header.banana":"yellow","noprefix":"oranges","wrongprefix.lemon":"yellow"], 
            headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green"
        headers.size() == 1
        
    }
    
    
    void "test null params"() {
        
        when:
        def headers = [:]
        def url = CamelUtils.generateUrlUsingHeadersAndQueryParams("http://localhost:1234/foo",
            null, 
            headers)
        
        then:
        url == "http://localhost:1234/foo"
        headers.size() == 0
        
    }
}

