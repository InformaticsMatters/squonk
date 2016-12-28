package org.squonk.core

import com.fasterxml.jackson.databind.ObjectMapper
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IOMultiplicity
import org.squonk.io.IORoute
import org.squonk.types.MoleculeObject
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
        def descriptor = new ServiceDescriptor(
                "cdk/logp",
                "CDK LogP",
                "CDK LogP predictions for XLogP and ALogP",
                ["logp", "partitioning", "cdk"] as String[],
                null,
                "icon.png",
                [IODescriptors.createMoleculeObjectDataset("input", IORoute.STREAM)] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output", IORoute.STREAM)] as IODescriptor[],
                null,
                null,
                "logp", // a URL relative to this URL?
        )

        when:
        def json = mapper.writeValueAsString(descriptor)
        println json
        def obj = mapper.readValue(json, ServiceDescriptor.class)

        then:
        json != null
        obj != null
        obj instanceof ServiceDescriptor
        obj.name == "CDK LogP"
    }

}

