package org.squonk.core

import com.fasterxml.jackson.databind.ObjectMapper
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
                MoleculeObject.class, // inputClass
                MoleculeObject.class, // outputClass
                ServiceDescriptor.DataType.STREAM, // inputTypes
                ServiceDescriptor.DataType.STREAM, // outputType
                "icon.png",
                "logp", // a URL relative to this URL?
                true,
                null,
                null
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

