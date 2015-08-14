package com.im.lac.services.job.service.adapters

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class HttpQueryParamsJobAdapterSpec extends Specification {
	
    
    void "test generate url"() {

        HttpQueryParamsJobAdapter adapter = new HttpQueryParamsJobAdapter()
        
        expect:
        adapter.generateUrl(b, p) == u
        
        where:
        b << [
            "http://localhost:1234/foo",
            "http://localhost:1234/foo",
            "http://localhost/foo",
            "http://localhost/foo"
        ]
        p << [
            [apple:"green",banana:"yellow"],
            [apple:"green",banana:"ye#low"],
            [apple:"green",banana:"yellow"],
            [:]
        ]
        u << [
            "http://localhost:1234/foo?apple=green&banana=yellow",
            "http://localhost:1234/foo?apple=green&banana=ye%23low",
            "http://localhost/foo?apple=green&banana=yellow",
            "http://localhost/foo"
        ]
        
        
    }

}

