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

package org.squonk.core.service.discovery

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.core.DockerServiceDescriptor
import org.squonk.core.HttpServiceDescriptor
import org.squonk.core.ServiceDescriptorSet
import org.squonk.util.ServiceConstants

import java.util.regex.Pattern

import static org.squonk.core.service.discovery.ServiceDiscoveryRouteBuilder.TEST_SERVICE_DESCRIPTORS

/**
 *
 * @author timbo
 */
class ServiceDiscoveryRouteBuilderSpec extends CamelSpecificationBase {

    @Override
    CamelContext createCamelContext() {
        SimpleRegistry reg = new SimpleRegistry();
        ServiceDescriptorRegistry descriptorReg = new ServiceDescriptorRegistry()
        reg.put(ServiceConstants.KEY_SERVICE_REGISTRY, descriptorReg);
        // push some dummy service descriptors. There is only one.
        descriptorReg.updateServiceDescriptorSet(new ServiceDescriptorSet("dummy", null, Arrays.asList(TEST_SERVICE_DESCRIPTORS)))
        new DefaultCamelContext(reg)
    }

    @Override
    RouteBuilder createRouteBuilder() {
        ServiceDiscoveryRouteBuilder rb = new ServiceDiscoveryRouteBuilder()
        rb.timerRepeats = 1

        return rb
    }

    void "pattern matcher"() {
        Pattern pat = Pattern.compile("(\\w+)/(.*)");

        when:
        def m = pat.matcher("pipelines/rdkit/foo.dsd")

        then:
        m.matches()
        m.group(1) == 'pipelines'

    }


    void "test read  descriptors"() {

        when:
        sleep(4000)
        ServiceDescriptorRegistry reg = camelContext.getRegistry().lookupByName(ServiceConstants.KEY_SERVICE_REGISTRY)
        def descs = reg.fetchServiceDescriptors()
        int http = 0
        int docker = 0
        boolean b = false
        descs.each() {
            if (it instanceof HttpServiceDescriptor) http++
            if (it instanceof DockerServiceDescriptor) docker++
            if (it.id == "test.noop") {
                b = true
            }
        }
        //println "HTTP: $http"
        //println "DOCKER: $docker"


        then:
        http > 0
        docker > 0
        b == true
    }
    
}

