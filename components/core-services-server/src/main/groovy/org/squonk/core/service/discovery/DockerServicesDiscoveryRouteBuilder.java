package org.squonk.core.service.discovery;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.execution.docker.DescriptorRegistry;
import org.squonk.execution.docker.DockerExecutorDescriptor;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import static org.squonk.core.CommonConstants.KEY_DOCKER_SERVICE_REGISTRY;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Extracts DockerExecutorDescriptors from ded files located in the directory specified by the SQUONK_DOCKER_SERVICES_DIR
 * environment variable.
 *
 * @author timbo
 */
public class DockerServicesDiscoveryRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(DockerServicesDiscoveryRouteBuilder.class.getName());

    protected String DOCKER_SERVICES_DIR = IOUtils.getConfiguration("SQUONK_DOCKER_SERVICES_DIR", "../../data/testfiles/docker-services");

    @Override
    public void configure() throws Exception {

        LOG.info("Using " + DOCKER_SERVICES_DIR + " for docker service discovery");

        from("file:" + DOCKER_SERVICES_DIR + "?antInclude=**/*.ded&noop=true&recursive=true&idempotent=true&delay=10000")
                .log("Processing file ${header.CamelFileName}")
                .process((exch) -> {
                    String filename = exch.getIn().getHeader("CamelFileName", String.class);
                    DescriptorRegistry reg = exch.getContext().getRegistry().lookupByNameAndType(KEY_DOCKER_SERVICE_REGISTRY, DescriptorRegistry.class);
                    InputStream is = exch.getIn().getBody(InputStream.class);
                    DockerExecutorDescriptor ded = JsonHandler.getInstance().objectFromJson(is, DockerExecutorDescriptor.class);
                    reg.add(ded.getId(), ded);
                    LOG.info("Discovered docker executor descriptor " + ded.getId() + " from file " + filename);
                });
    }

}
