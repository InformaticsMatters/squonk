package org.squonk.core.service.discovery

import org.squonk.core.HttpServiceDescriptor
import org.squonk.core.ServiceConfig
import org.squonk.core.ServiceDescriptor
import org.squonk.core.ServiceDescriptorSet
import org.squonk.io.IODescriptor
import org.squonk.io.IOMultiplicity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 29/11/16.
 */
@Stepwise
class PostgresServiceDescriptorClientSpec extends Specification {

    @Shared
    PostgresServiceDescriptorClient client = new PostgresServiceDescriptorClient()


    static ServiceDescriptorSet set1 = new ServiceDescriptorSet("http://somewhere.com/baseurl/1", "http://somewhere.com/healthurl",
            [
                    createServiceDescriptor("id1"),
                    createServiceDescriptor("id2"),
                    createServiceDescriptor("id3"),
            ])

    static ServiceDescriptorSet set2 = new ServiceDescriptorSet("http://somewhere.com/baseurl/2", "http://somewhere.com/healthurl",
            [
                    createServiceDescriptor("id1"),
                    createServiceDescriptor("id2"),
                    createServiceDescriptor("id3"),
            ])


    /*
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor inputDescriptor,
            IODescriptor outputDescriptor,
            OptionDescriptor[] options,
            String executorClassName,
            String executionEndpoint
     */
    static HttpServiceDescriptor createServiceDescriptor(String id) {
        return new HttpServiceDescriptor(id, id, id, ["t1", "t2"] as String[], "resourceurl",
                "icon",
                new IODescriptor("input", "text/plain", String.class, null, IOMultiplicity.ITEM),
                new IODescriptor("output", "text/plain", String.class, null, IOMultiplicity.ITEM),
                null, null, "executor")
    }

    void setupSpec() {
        client.sql.executeUpdate("DELETE FROM users.service_descriptors")
        client.sql.executeUpdate("DELETE FROM users.service_descriptor_sets")
    }

    void "initial counts zero"() {

        when:
        int c1 = client.countServiceDescriptorSets()
        int c2 = client.countServiceDescriptors()

        then:
        c1 == 0
        c2 == 0
    }

    void "test initial insert"() {
        List sdsets = [set1, set2]

        when:
        client.update(sdsets)
        int c1 = client.countServiceDescriptorSets()
        int c2 = client.countServiceDescriptors()

        then:
        c1 == 2
        c2 == 6

    }


    void "test update"() {
        List sdsets = [set1]

        when:
        client.update(sdsets)
        int c1 = client.countServiceDescriptorSets()
        int c2 = client.countServiceDescriptors()

        then:
        c1 == 2
        c2 == 6

    }

    void "test list"() {

        when:
        def list = client.list()

        then:
        list.size() == 2
        list[0].asServiceConfigs.size() == 3
        list[1].asServiceConfigs.size() == 3
        list[0].baseUrl == "http://somewhere.com/baseurl/1"
        list[1].baseUrl == "http://somewhere.com/baseurl/2"

    }

    void "test fetch one"() {

        when:
        def list = client.fetch("http://somewhere.com/baseurl/1")

        then:
        list.size() == 3

    }

}
