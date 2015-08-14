package com.im.lac.services.job.service.adapters

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class HttpGenericParamsJobAdapterSpec extends Specification {
	
    
    void "test querty params"() {

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter()
        
        when:
        def headers = [:]
        def url = adapter.generateUrl("http://localhost:1234/foo", ["query.apple":"green","query.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green&banana=yellow"
        headers.size() == 0
        
    }
    
    void "test header params"() {

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter()
        
        when:
        def headers = [:]
        def url = adapter.generateUrl("http://localhost:1234/foo", ["header.apple":"green","header.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo"
        headers.size() == 2
        
    }
    
    
    void "test mixed params"() {

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter()
        
        when:
        def headers = [:]
        def url = adapter.generateUrl("http://localhost:1234/foo", ["query.apple":"green","header.banana":"yellow"], headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green"
        headers.size() == 1
        
    }
    
    
    void "test skipped params"() {

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter()
        
        when:
        def headers = [:]
        def url = adapter.generateUrl("http://localhost:1234/foo",
            ["query.apple":"green","header.banana":"yellow","noprefix":"oranges","wrongprefix.lemon":"yellow"], 
            headers)
        
        then:
        url == "http://localhost:1234/foo?apple=green"
        headers.size() == 1
        
    }
    
    
    void "test null params"() {

        HttpGenericParamsJobAdapter adapter = new HttpGenericParamsJobAdapter()
        
        when:
        def headers = [:]
        def url = adapter.generateUrl("http://localhost:1234/foo",
            null, 
            headers)
        
        then:
        url == "http://localhost:1234/foo"
        headers.size() == 0
        
    }



}

