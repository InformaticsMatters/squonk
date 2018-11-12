package org.squonk.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import groovy.io.FileType
import org.squonk.types.io.JsonHandler
import spock.lang.Specification
import spock.lang.Ignore

/**
 * Created by timbo on 02/08/17.
 */
class NextflowServiceDescriptorSpec extends Specification {

    def "read json"() {

        ObjectMapper mapper1 = new ObjectMapper()

        when:
        def nsd = mapper1.readValue(new File("src/test/groovy/org/squonk/core/nextflow1.nsd.json"), NextflowServiceDescriptor.class)

        then:
        nsd != null
    }

    def "read yaml"() {

        ObjectMapper mapper1 = new ObjectMapper(new YAMLFactory());

        when:
        def nsd = mapper1.readValue(new File("src/test/groovy/org/squonk/core/nextflow1.nsd.yml"), NextflowServiceDescriptor.class)

        then:
        nsd != null
        nsd.nextflowFile.contains("printf 'Hello world! \\n'")

    }

}
