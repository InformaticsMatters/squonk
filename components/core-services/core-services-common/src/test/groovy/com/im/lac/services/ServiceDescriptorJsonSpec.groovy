package com.im.lac.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.im.lac.dataset.Metadata
import spock.lang.Specification

import com.im.lac.job.jobdef.*
import com.im.lac.services.AccessMode
import com.im.lac.services.ServiceDescriptor
import com.im.lac.types.MoleculeObject

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
            ["/Chemistry/Toolkits/CDK/Calculators", "Chemistry/Calculators/Partioning"] as String[],
            "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
            null,
            ["public"] as String[],
            [MoleculeObject.class] as Class[], // inputClasses
            [MoleculeObject.class] as Class[], // outputClasses
            [Metadata.Type.ARRAY] as Metadata.Type[], // inputTypes
            [Metadata.Type.ARRAY] as Metadata.Type[], // outputTypes
            [new AccessMode(
                "async",
                "Immediate execution",
                "Execute as an asynchronous REST web service",
                "logp", // a URL relative to this URL?
                true,
                AsyncHttpProcessDatasetJobDefinition.class,
                0, Integer.MAX_VALUE, 0.001f,
                [ServiceDescriptor.LicenseToken.CHEMAXON] as ServiceDescriptor.LicenseToken[])
            ] as AccessMode[])
        
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

