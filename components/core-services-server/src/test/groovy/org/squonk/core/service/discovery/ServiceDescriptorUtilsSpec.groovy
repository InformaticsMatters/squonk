package org.squonk.core.service.discovery

import org.squonk.core.AccessMode
import org.squonk.core.ServiceDescriptor
import spock.lang.Specification
import static org.squonk.core.service.discovery.ServiceDiscoveryRouteBuilder.TEST_SERVICE_DESCRIPTORS
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

    void "copy props when making absolute"() {
        ServiceDescriptor sd1 = TEST_SERVICE_DESCRIPTORS[0]

        when:
        ServiceDescriptor sd2 = ServiceDescriptorUtils.makeAbsolute("http://nowhere.com/", sd1)

        then:
        sd2.getId() != null
        sd2.getIcon() != null
        sd2.getAccessModes()[0].getExecutionEndpoint().startsWith("http://nowhere.com/")
    }

}

