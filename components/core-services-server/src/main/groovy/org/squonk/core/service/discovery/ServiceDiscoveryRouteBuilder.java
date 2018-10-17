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

package org.squonk.core.service.discovery;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.squonk.core.*;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.ServiceConstants;

import javax.activation.DataHandler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * Services related to handling ServiceDescriptors.
 * Allows new services to be registered and for the registered services to be queried e.g. by the portal application.
 *
 *
 * @author timbo
 */
public class ServiceDiscoveryRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ServiceDiscoveryRouteBuilder.class.getName());

    public static final String ROUTE_REQUEST_SERVICE_CONFIGS = "direct:request_service_configs";
    public static final String ROUTE_REQUEST_SERVICE_DESCRIPTORS = "direct:request_service_descriptors";
    public static final String ROUTE_POST_SD_SET = "direct:post-service-descriptor-set";
    public static final String ROUTE_POST_SD_SINGLE = "direct:post-service-descriptor-single";
    private static Pattern EXECUTOR_PATTERN = Pattern.compile("/(\\w+)/(.*)");
    protected String DOCKER_SERVICES_DIR = IOUtils.getConfiguration("SQUONK_DOCKER_SERVICES_DIR", "../../data/testfiles/docker-services");


    /**
     * This allows the timer to be turned off or set to only run a certain
     * number of times, primarily to allow easy testing
     */
    protected int timerRepeats = 0;

    /**
     * This allows the timer delay to be set, primarily to allow easy testing.
     * Default is to reload every 15 mins after an initial 2 min delay. For testing when you want to push new service
     * descriptors quickly set the SQUONK_SERVICE_DISCOVERY_INTERVAL environment variable to a shorter interval
     * e.g. 60000 for every minute.
     */
    protected int defaultTimerInteval = 15 * 60 * 1000;
    protected String timerInterval = IOUtils.getConfiguration("SQUONK_SERVICE_DISCOVERY_INTERVAL", String.valueOf(defaultTimerInteval));
    protected int timerDelay = 2 * 60 * 1000;

    private final Map<String, String> locations = new LinkedHashMap<>();

    public ServiceDiscoveryRouteBuilder() {
        LOG.info("Services will be looked for in " + DOCKER_SERVICES_DIR);
    }

    public static final HttpServiceDescriptor[] TEST_SERVICE_DESCRIPTORS = new HttpServiceDescriptor[]{
            new HttpServiceDescriptor(
                    "test.noop",
                    "NOOP Service",
                    "Does nothing other than submit a Job",
                    new String[]{"testing"},
                    "http://foo.com/something",
                    "default_icon.png",
                    new IODescriptor[]{IODescriptors.createMoleculeObjectDataset("input")},
                    new IODescriptor[]{IODescriptors.createMoleculeObjectDataset("output")},
                    null, // options
                    "executor",
                    "valueIsIgnored" // endpoint
            )
    };

    @Override
    public void configure() throws Exception {

        from(ROUTE_REQUEST_SERVICE_CONFIGS)
                .log("ROUTE_REQUEST")
                .process((Exchange exch) -> {
                    ServiceDescriptorRegistry reg = fetchDescriptorRegistry(exch.getContext());
                    reg.init(); // this ensures the descriptors are loaded from the DB before we update anything
                    List<ServiceConfig> list = reg.fetchServiceConfigs();
                    exch.getIn().setBody(list);
                });

        from(ROUTE_REQUEST_SERVICE_DESCRIPTORS)
                .log("ROUTE_REQUEST")
                .process((Exchange exch) -> {
                    ServiceDescriptorRegistry reg = fetchDescriptorRegistry(exch.getContext());
                    reg.init(); // this ensures the descriptors are loaded from the DB before we update anything
                    List<ServiceDescriptor> list = reg.fetchServiceDescriptors();
                    exch.getIn().setBody(list);
                });

        /** Handles the posting of a ServiceDescriptorSet in JSON format.
         * The execution endpoint of the contained ServiceDescriptors will be expanded using the baseUrl property of the
         * ServiceDescriptorSet if it does not already start with 'http'.
         *
         */
        from(ROUTE_POST_SD_SET)
                .log("ROUTE_POST_SD_SET")
                .process((Exchange exch) -> {
                    String contentType = exch.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
                    String content = exch.getIn().getBody(String.class);
                    ServiceDescriptorSet sdset;
                    if (CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_SET_JSON.equals(contentType)) {
                        sdset = ServiceDescriptorUtils.readJson(content, ServiceDescriptorSet.class);
                    } else if (CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_SET_YAML.equals(contentType)) {
                        sdset = ServiceDescriptorUtils.readYaml(content, ServiceDescriptorSet.class);
                    } else {
                        throw new IllegalStateException("Unsupported Content-Type. Received " + contentType +
                                " but expected " + CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_SET_JSON + " or " +
                                CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_SET_YAML
                        );
                    }
                    ServiceDescriptorUtils.processServiceDescriptorRequest(sdset);
                    ServiceDescriptorRegistry reg = fetchDescriptorRegistry(exch.getContext());
                    reg.init(); // this ensures the descriptors are loaded from the DB before we update anything
                    reg.updateServiceDescriptorSet(sdset);
                    // set a message as the response
                    exch.getOut().setBody("Loaded " + sdset.getServiceDescriptors().size() + " service descriptors from " + sdset.getBaseUrl());
                });

        /** Handles POSTing of a ServiceDescriptor such as a @{link NextFlowServiceDescriptor} or
         * @{link DockerServiceDescriptor} that can be composed from multiple parts that need to be combined to generate
         * the final ServiceDescriptor
         *
         */
        from(ROUTE_POST_SD_SINGLE)
                .log("ROUTE_POST_SD_SINGLE")
                .process((Exchange exch) -> {
                    String baseUrl = exch.getIn().getHeader("Base-URL", String.class);
                    if (baseUrl == null) {
                        throw new RuntimeException("Failed to read Base-URL header");
                    }
                    LOG.info("BaseUrl is " + baseUrl);

                    ServiceDescriptor sd = readMultipartServiceDescriptor(exch.getIn());
                    if (sd == null) {
                        throw new RuntimeException("Failed to read service descriptor");
                    }

                    ServiceDescriptorRegistry reg = fetchDescriptorRegistry(exch.getContext());
                    reg.init(); // this ensures the descriptors are loaded from the DB before we update anything

                    // this updates or creates the ServiceDescriptorSet
                    LOG.info("Updating ServiceDescriptor " + sd.getId() + " for " + baseUrl);
                    ServiceDescriptorSet sds = reg.updateServiceDescriptor(baseUrl, sd);
                    // and now persist the update
                    reg.updateServiceDescriptorSet(sds);
                    LOG.info("Set " + baseUrl + " is now of size " + reg.fetchServiceDescriptorSet(baseUrl).getServiceDescriptors().size());


                })
                .transform(constant("OK\n"));


        // This checks the currently available services on a scheduled basis
        from("timer:discover?period=" + timerInterval + "&repeatCount=" + timerRepeats + "&delay=" + timerDelay)
                .log("UPDATE_SERVICES")
                .process(exch -> {
                    ServiceDescriptorRegistry reg = fetchDescriptorRegistry(exch.getContext());
                    reg.init(); // this ensures the previous descriptors are loaded from the DB before we update anything

                    // check the health of the services with health checks
                    LOG.fine("Checking status of services for " + locations.size() + " locations");
                    try {
                        performHealthCheck(reg);
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Failed to update HTTP services", ex);
                    }
                    LOG.fine("Updating services complete: " + reg.fetchServiceDescriptorSets().size());
                });
    }

    protected void performHealthCheck(ServiceDescriptorRegistry reg) {
        reg.fetchExternalServiceDescriptorSets().forEach((sds) -> {
            LOG.info("Checking status of services for " + sds.getBaseUrl());
            Date now = new Date();
            int active = 0;
            int inactive = 0;
            int unknown = 0;

            checkHealth(sds, now);

            for (ServiceDescriptor sd : sds.getServiceDescriptors()) {
                ServiceConfig.Status status = sd.getServiceConfig().getStatus();
                if (status == null || status.equals(ServiceConfig.Status.UNKNOWN)) {
                    unknown++;
                } else if (status == ServiceConfig.Status.ACTIVE) {
                    active++;
                } else if (status == ServiceConfig.Status.INACTIVE) {
                    inactive++;
                }
            }
            // this saves the changes to the DB
            reg.updateServiceDescriptorSet(sds);
            LOG.info("Updated services for " + sds.getBaseUrl() + ". " + active + " active, " +
                    inactive + " inactive and " + unknown + " unknown services defined");
        });
    }

    private ServiceDescriptorRegistry fetchDescriptorRegistry(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(ServiceConstants.KEY_SERVICE_REGISTRY, ServiceDescriptorRegistry.class);
    }

    private void checkHealth(ServiceDescriptorSet sds, Date now) {

        String healthUrl = sds.getHealthUrl();
        if (healthUrl == null) {
            return;
        }
        boolean b = true;
        try {
            URL url = new URL(healthUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int code = connection.getResponseCode();
            b = code == 200;
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Health check failed for " + healthUrl, ex);
        }

        ServiceConfig.Status status = (b ? ServiceConfig.Status.ACTIVE : ServiceConfig.Status.INACTIVE);
        List<ServiceDescriptor> serviceDescriptors = sds.getServiceDescriptors();
        LOG.fine("Setting status of " + serviceDescriptors.size() + " service descriptors from " + sds.getBaseUrl() +
                " with health URL of " + healthUrl + " to " + status);
        serviceDescriptors.forEach(sd -> {
            LOG.finer("Setting status of ServiceDescriptor " + sd.getId() + " to " + status);
            sd.getServiceConfig().setStatus(status);
            sd.getServiceConfig().setStatusLastChecked(now);
        });
    }

    private ServiceDescriptor readMultipartServiceDescriptor(Message message) throws IOException {
        String contentType = message.getHeader(Exchange.CONTENT_TYPE, String.class);
        LOG.info(String.format("Body Content-Type: %s", contentType));

        String body = message.getBody(String.class);
        //LOG.info("BODY:\n" + body);
        if (body == null || body.isEmpty()) {
            throw new IllegalStateException("No service descriptor posted");
        }


        // the first attachment is the second form field etc.
        if (contentType.toLowerCase().startsWith(CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW + "+")) {
            LOG.info("Looks like a Nextflow service descriptor");
            NextflowServiceDescriptor nsd;
            if (contentType.equals(CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW_JSON)) {
                nsd = ServiceDescriptorUtils.readJson(body, NextflowServiceDescriptor.class);
            } else if (contentType.equals(CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW_YAML)) {
                nsd = ServiceDescriptorUtils.readYaml(body, NextflowServiceDescriptor.class);
            } else {
                throw new IllegalStateException("Content-Type " + contentType + " not supported for service descriptors");
            }
            for (Map.Entry<String, DataHandler> e : message.getAttachments().entrySet()) {
                LOG.info("Reading attachment " + e.getKey());
                if ("nextflow.nf".equalsIgnoreCase(e.getKey())) {
                    String nextflowFile = IOUtils.convertStreamToString(e.getValue().getInputStream());
                    if (nextflowFile == null || nextflowFile.isEmpty()) {
                        throw new IllegalStateException("Nextflow file field specified but no content found");
                    }
                    nsd.setNextflowFile(nextflowFile);
                } else if ("nextflow.config".equalsIgnoreCase(e.getKey())) {
                    String nextflowConfig = IOUtils.convertStreamToString(e.getValue().getInputStream());
                    if (nextflowConfig == null || nextflowConfig.isEmpty()) {
                        throw new IllegalStateException("Nextflow config field specified but no content found");
                    }
                    nsd.setNextflowConfig(nextflowConfig);
                }
            }
            return nsd;
        } else if (contentType.toLowerCase().startsWith(CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER + "+")) {
            LOG.info("Looks like a Docker service descriptor");
            DockerServiceDescriptor dsd;
            if (contentType.equals(CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER_JSON)) {
                dsd = ServiceDescriptorUtils.readJson(body, DockerServiceDescriptor.class);
            } else if (contentType.equals(CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER_YAML)) {
                dsd = ServiceDescriptorUtils.readYaml(body, DockerServiceDescriptor.class);
            } else {
                throw new IllegalStateException("Content-Type " + contentType + " not supported for service descriptors");
            }
            return dsd;
        } else {
            LOG.warning("Unsupported service descriptor type: " + contentType);
        }
        return null;
    }

}
