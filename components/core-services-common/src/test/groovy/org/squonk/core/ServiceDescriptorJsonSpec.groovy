/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.io.FileType
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.types.io.JsonHandler
import spock.lang.Specification
import spock.lang.Ignore

/**
 *
 * @author timbo
 */
class ServiceDescriptorJsonSpec extends Specification {


    void "ServiceDescriptor json"() {
        setup:
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
        def obj = mapper.readValue(json, ServiceDescriptor.class)

        then:
        json != null
        obj != null
        obj instanceof HttpServiceDescriptor
        obj.serviceConfig.name == "CDK LogP"
        obj.executionEndpoint != null
    }

    void "ServiceDescriptor list"() {
        setup:
        ObjectMapper mapper = new ObjectMapper()
        def descriptor1 = new HttpServiceDescriptor(
                "cdk/logp/1",
                "CDK LogP",
                "CDK LogP predictions for XLogP and ALogP",
                ["logp", "partitioning", "cdk"] as String[],
                null,
                "icon.png",
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null,
                null,
                "logp1", // a URL relative to this URL?
        )
        def descriptor2 = new HttpServiceDescriptor(
                "cdk/logp/2",
                "CDK LogP",
                "CDK LogP predictions for XLogP and ALogP",
                ["logp", "partitioning", "cdk"] as String[],
                null,
                "icon.png",
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null,
                null,
                "logp2", // a URL relative to this URL?
        )

        def list = [descriptor1, descriptor2]

        when:
        def json1 = mapper.writeValueAsString(descriptor1)
        def json2 = mapper.writeValueAsString(descriptor2)
        def json = "[$json1,$json2]"
        println json
        def obj = mapper.readValue(json, List.class)

        then:
        json != null
        obj != null
        obj instanceof List
        obj.size() == 2
    }


    @Ignore
    void "validate pipelines docker service descriptors"() {

        def list = []
        def dir = new File("../../data/testfiles/docker-services/")
        dir.eachFileRecurse (FileType.FILES) { file ->
            if (file.getName().endsWith(".dsd.json"))
            list << file
        }

        when:

        def descriptors = []
        int errors = 0
        list.each { file ->
            println "Trying $file"
            try {
                descriptors << JsonHandler.getInstance().objectFromJson(new FileInputStream(file), DockerServiceDescriptor.class)
            } catch (IOException ex) {
                errors++
                println "Failed to read $file"
            }
        }
        println "Read ${descriptors.size()} docker descriptors"


        then:
        descriptors.size() > 0
        errors == 0

    }
}

