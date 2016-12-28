package org.squonk.execution.docker.impl

import org.squonk.execution.docker.DockerExecutorDescriptor
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 06/12/16.
 */
class RDKitDockerExecutorDescriptorsSpec extends Specification {

    void "test read resource"() {
        when:
        InputStream is = getClass().getResourceAsStream("foo.txt")

        then:
        is != null
    }

    void "read/write json"() {
        def all = Arrays.asList(RDKitDockerExecutorDescriptors.getAll())
        println "Found ${all.size()} descriptors"
        //def stream1 = all.stream()
        when:
        //InputStream is = JsonHandler.getInstance().marshalStreamToJsonArray(stream1, false)
        //stream1.close()

        String json =JsonHandler.getInstance().objectToJson(all)
        println json
        Stream stream2 = JsonHandler.getInstance().streamFromJson(json, DockerExecutorDescriptor.class)
        long size = stream2.count()
        println "Unmarshalled $size descriptors"

        then:
        size > 0

    }
}
