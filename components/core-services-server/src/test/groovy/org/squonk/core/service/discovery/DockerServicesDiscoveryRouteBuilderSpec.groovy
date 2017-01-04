package org.squonk.core.service.discovery

import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import org.squonk.camel.testsupport.CamelSpecificationBase
import org.squonk.core.CommonConstants
import org.squonk.execution.docker.DescriptorRegistry

/**
 * Created by timbo on 28/12/16.
 */
class DockerServicesDiscoveryRouteBuilderSpec extends CamelSpecificationBase {

    @Override
    RouteBuilder createRouteBuilder() {
        return new DockerServicesDiscoveryRouteBuilder()
    }

    @Override
    CamelContext createCamelContext() {
        SimpleRegistry reg = new SimpleRegistry();
        reg.put(CommonConstants.KEY_DOCKER_SERVICE_REGISTRY, new DescriptorRegistry());
        new DefaultCamelContext(reg)
    }

    void "test read descriptors"() {

        when:
        sleep(2500)
        DescriptorRegistry reg = camelContext.getRegistry().lookupByName(CommonConstants.KEY_DOCKER_SERVICE_REGISTRY)


        then:
        reg.fetchDescriptors().size() > 0
    }
}
