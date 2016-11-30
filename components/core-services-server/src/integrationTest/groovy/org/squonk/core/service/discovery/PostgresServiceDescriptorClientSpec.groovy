package org.squonk.core.service.discovery

import org.squonk.core.ServiceDescriptor
import org.squonk.core.ServiceDescriptorSet
import org.squonk.options.OptionDescriptor
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
            Class inputClass,
            Class outputClass,
            DataType inputType,
            DataType outputType,
            String icon,
            String executionEndpoint,
            boolean endpointRelative,
            OptionDescriptor[] options,
            String executorClassName
     */
    static ServiceDescriptor createServiceDescriptor(String id) {
        return new ServiceDescriptor(id, id, id, ["t1", "t2"] as String[], "resourceurl",
                String.class, String.class,
                ServiceDescriptor.DataType.ITEM, ServiceDescriptor.DataType.ITEM,
                "icon", "foo/bar", true, null, "executor")
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
        list[0].serviceDescriptors.size() == 3
        list[1].serviceDescriptors.size() == 3
        list[0].baseUrl == "http://somewhere.com/baseurl/1"
        list[1].baseUrl == "http://somewhere.com/baseurl/2"

    }


}
