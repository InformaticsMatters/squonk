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

    @Ignore
    void "validate nextflow service descriptors"() {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        def list = []
        def dir = new File("../../data/testfiles/docker-services/")
        dir.eachFileRecurse (FileType.FILES) { file ->
            if (file.getName().endsWith(".nsd.yml"))
                list << file
        }

        when:

        def descriptors = []
        int errors = 0
        list.each { file ->
            println "Trying $file"
            try {
                descriptors << mapper.readValue(new FileInputStream(file), NextflowServiceDescriptor.class)
            } catch (IOException ex) {
                errors++
                println "Failed to read $file"
                ex.printStackTrace()
            }
        }
        println "Read ${descriptors.size()} nextflow descriptors"


        then:
        descriptors.size() > 0
        errors == 0

    }


}
