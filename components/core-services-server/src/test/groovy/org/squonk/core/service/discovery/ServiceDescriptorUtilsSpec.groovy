package org.squonk.core.service.discovery

import org.squonk.core.ServiceDescriptor
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ServiceDescriptorUtilsSpec extends Specification {

    void "relative with trailing slash"() {
        setup:
        ServiceDescriptor sd = createServiceDescriptor("foo", true)

        when:
        String url = ServiceDescriptorUtils.makeAbsoluteUrl("http://localhost:8080/some/path/", sd)

        then:
        url == "http://localhost:8080/some/path/foo"
    }

    void "relative without trailing slash"() {
        setup:
        ServiceDescriptor sd = createServiceDescriptor("foo", true)

        when:
        String url = ServiceDescriptorUtils.makeAbsoluteUrl("http://localhost:8080/some/path", sd)

        then:
        url == "http://localhost:8080/some/path/foo"
    }

    void "absolute url"() {
        setup:
        ServiceDescriptor sd = createServiceDescriptor("http://localhost:8080/some/other/path", false)

        when:
        String url = ServiceDescriptorUtils.makeAbsoluteUrl("http://localhost:8080/some/path", sd)

        then:
        url == "http://localhost:8080/some/other/path"
    }

    void "copy props when making absolute"() {
        ServiceDescriptor sd1 = createServiceDescriptor("foo", true)

        when:
        ServiceDescriptor sd2 = ServiceDescriptorUtils.makeAbsolute("http://nowhere.com/", sd1)

        then:
        sd2.id == "id"
        sd2.icon == "icon.png"
        sd2.getExecutionEndpoint().startsWith("http://nowhere.com/")
    }

    private ServiceDescriptor createServiceDescriptor(String endpoint, boolean isRelative) {
        return new ServiceDescriptor("id", "name", "desc", null, null, null, null, null, null, "icon.png",
                endpoint, isRelative,
                null, null
        )
    }

}

