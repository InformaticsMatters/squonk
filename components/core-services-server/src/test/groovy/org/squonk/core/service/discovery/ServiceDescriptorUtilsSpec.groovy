package org.squonk.core.service.discovery

import org.squonk.core.HttpServiceDescriptor
import org.squonk.io.IODescriptor
import spock.lang.Specification

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

/**
 *
 * @author timbo
 */
class ServiceDescriptorUtilsSpec extends Specification {

    void "relative with trailing slash"() {
        setup:
        HttpServiceDescriptor sd = createServiceDescriptor("foo")

        when:
        String url = ServiceDescriptorUtils.makeAbsoluteUrl("http://localhost:8080/some/path/", sd)

        then:
        url == "http://localhost:8080/some/path/foo"
    }

    void "relative without trailing slash"() {
        setup:
        HttpServiceDescriptor sd = createServiceDescriptor("foo",)

        when:
        String url = ServiceDescriptorUtils.makeAbsoluteUrl("http://localhost:8080/some/path", sd)

        then:
        url == "http://localhost:8080/some/path/foo"
    }

    void "absolute url"() {
        setup:
        HttpServiceDescriptor sd = createServiceDescriptor("http://localhost:8080/some/other/path")

        when:
        String url = ServiceDescriptorUtils.makeAbsoluteUrl("http://localhost:8080/some/path", sd)

        then:
        url == "http://localhost:8080/some/other/path"
    }

    void "copy props when making absolute"() {
        HttpServiceDescriptor sd1 = createServiceDescriptor("foo")

        when:
        HttpServiceDescriptor sd2 = ServiceDescriptorUtils.makeAbsolute("http://nowhere.com/", sd1)

        then:
        sd2.id == "id"
        sd2.serviceConfig.icon == "icon.png"
        sd2.getExecutionEndpoint().startsWith("http://nowhere.com/")
    }

    private HttpServiceDescriptor createServiceDescriptor(String endpoint) {
        return new HttpServiceDescriptor("id", "name", "desc", null, null, "icon.png", new IODescriptor[0], new IODescriptor[0], null, null, endpoint)
    }


    void "walk tree"() {
        when:
        Stream paths = Files.walk(FileSystems.getDefault().getPath("../../data/testfiles/docker-services"))
        long count = paths.peek() {
            println it
        }.count()

        then:
        count > 0

    }
}

