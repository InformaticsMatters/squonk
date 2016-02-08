package org.squonk.core.discovery.service

import org.squonk.core.AccessMode
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ServiceDescriptorUtilsSpec extends Specification {
	
    void "relative with trailing slash"() {
        setup:
        AccessMode mode = new AccessMode(null, null, null,"foo", true, null, 0, 1, 0f, null, null, null)
        
        when:
        String url = ServiceDescriptorUtils.getAbsoluteUrl("http:localhost:8080/some/path/", mode)
        
        then:
        url == "http:localhost:8080/some/path/foo"
    }
    
    void "relative without trailing slash"() {
        setup:
        AccessMode mode = new AccessMode(null, null, null,"foo", true, null, 0, 1, 0f, null, null, null)
        
        when:
        String url = ServiceDescriptorUtils.getAbsoluteUrl("http:localhost:8080/some/path", mode)
        
        then:
        url == "http:localhost:8080/some/path/foo"
    }
    
    void "absolute url"() {
        setup:
        AccessMode mode = new AccessMode(null, null, null,"http:localhost:8080/some/other/path", false, null, 0, 1, 0f, null, null, null)
        
        when:
        String url = ServiceDescriptorUtils.getAbsoluteUrl("http:localhost:8080/some/path", mode)
        
        then:
        url == "http:localhost:8080/some/other/path"
    }
}

