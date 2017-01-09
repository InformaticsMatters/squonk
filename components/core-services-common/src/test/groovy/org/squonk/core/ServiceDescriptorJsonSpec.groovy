package org.squonk.core

import com.fasterxml.jackson.databind.ObjectMapper
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
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
        obj.name == "CDK LogP"
    }

}

