package org.squonk.core

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.io.FileType
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ServiceDescriptorJsonSpec extends Specification {


    void "ServiceDescriptor json"() {
        setup:
        println "ServiceDescriptor json()"
        ObjectMapper mapper = new ObjectMapper()
        def descriptor = new HttpServiceDescriptor(
                "cdk/logp",
                "CDK LogP",
                "CDK LogP predictions for XLogP and ALogP",
                ["logp", "partitioning", "cdk"] as String[],
                null,
                "icon.png",
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null,
                null,
                "logp", // a URL relative to this URL?
        )

        when:
        def json = mapper.writeValueAsString(descriptor)
        println json
        def obj = mapper.readValue(json, HttpServiceDescriptor.class)

        then:
        json != null
        obj != null
        obj instanceof HttpServiceDescriptor
        obj.serviceConfig.name == "CDK LogP"
        obj.executionEndpoint != null
    }

    void "docker service descriptor"() {

        def list = []
        def dir = new File("../../data/testfiles/docker-services/")
        dir.eachFileRecurse (FileType.FILES) { file ->
            if (file.getName().endsWith(".dsd"))
            list << file
        }

        when:

        def descriptors = []
        list.each { file ->
            println "Trying $file"
            descriptors << JsonHandler.getInstance().objectFromJson(new FileInputStream(file), DockerServiceDescriptor.class)
        }


        then:
        descriptors.size() > 0



    }



}

