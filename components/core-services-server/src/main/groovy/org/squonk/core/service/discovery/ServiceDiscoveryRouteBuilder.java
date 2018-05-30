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
import org.apache.camel.builder.RouteBuilder;
import org.squonk.core.*;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.util.IOUtils;
import org.squonk.util.ServiceConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author timbo
 */
public class ServiceDiscoveryRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ServiceDiscoveryRouteBuilder.class.getName());

    public static final String ROUTE_REQUEST = "direct:request";
    private static Pattern EXECUTOR_PATTERN = Pattern.compile("/(\\w+)/(.*)");
    protected String DOCKER_SERVICES_DIR = IOUtils.getConfiguration("SQUONK_DOCKER_SERVICES_DIR", "../../data/testfiles/docker-services");




    /**
     * This allows the timer to be turned off or set to only run a certain
     * number of times, primarily to allow easy testing
     */
    protected int timerRepeats = 0;

    /**
     * This allows the timer delay to be set, primarily to allow easy testing.
     * Default is to reload every 15 mins. For testing when you want to push new service descriptors quickly set the
     * SQUONK_SERVICE_DISCOVERY_INTERVAL environment variable to a shorter interval e.g. 60000 for every minute.
     */
    protected int defaultTimerDelay = 15 * 60 * 1000;
    protected String timerDelay = IOUtils.getConfiguration("SQUONK_SERVICE_DISCOVERY_INTERVAL", String.valueOf(defaultTimerDelay));

    private final Map<String, String> locations = new LinkedHashMap<>();

    public ServiceDiscoveryRouteBuilder() {

        String basicChemServicesUrl = IOUtils.getConfiguration("SQUONK_BASIC_CHEM_SERVICES_URL", null);

        if (basicChemServicesUrl != null) {
            LOG.info("Enabling basic chem services from " + basicChemServicesUrl);

            locations.put(basicChemServicesUrl + "/chem-services-cdk-basic/rest/v1/calculators", basicChemServicesUrl + "/chem-services-cdk-basic/rest/ping");
            locations.put(basicChemServicesUrl + "/chem-services-cdk-basic/rest/v1/converters", basicChemServicesUrl + "/chem-services-cdk-basic/rest/ping");
            locations.put(basicChemServicesUrl + "/chem-services-chemaxon-basic/rest/v1/calculators", basicChemServicesUrl + "/chem-services-chemaxon-basic/rest/ping");
            locations.put(basicChemServicesUrl + "/chem-services-chemaxon-basic/rest/v1/descriptors", basicChemServicesUrl + "/chem-services-chemaxon-basic/rest/ping");
            locations.put(basicChemServicesUrl + "/chem-services-rdkit-basic/rest/v1/calculators", basicChemServicesUrl + "/chem-services-rdkit-basic/rest/ping");
            locations.put(basicChemServicesUrl + "/chem-services-rdkit-search/rest/v1/db", basicChemServicesUrl + "/chem-services-rdkit-search/rest/ping");
            locations.put(basicChemServicesUrl + "/chem-services-openchemlib-basic/rest/v1/calculators", basicChemServicesUrl + "/chem-services-openchemlib-basic/rest/ping");
            locations.put(basicChemServicesUrl + "/chem-services-smartcyp/rest/v1", basicChemServicesUrl + "/chem-services-smartcyp/rest/ping");
            //locations.put(basicChemServicesUrl + "/chem-services-cpsign/rest/v1", basicChemServicesUrl + "/chem-services-cpsign/rest/ping");

        } else {
            LOG.warning("Environment variable SQUONK_BASIC_CHEM_SERVICES_URL not defined. Basic Chem services will not be available");
        }

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

        from(ROUTE_REQUEST)
                .log("ROUTE_REQUEST")
                .process((Exchange exch) -> {
                    ServiceDescriptorRegistry reg = fetchDescriptorRegistry(exch.getContext());
                    reg.init(); // this ensures the descriptors are loaded from the DB before we update anything
                    List<ServiceConfig> list = reg.fetchServiceConfigs();
                    exch.getIn().setBody(list);
                });

        // This updates the currently available services on a scheduled basis
        from("timer:discover?period=" + timerDelay + "&repeatCount=" + timerRepeats)
                .log("UPDATE_SERVICES")
                .process(exch -> {
                    ServiceDescriptorRegistry reg = fetchDescriptorRegistry(exch.getContext());
                    reg.init(); // this ensures the previous descriptors are loaded from the DB before we update anything
                    // update the docker services
                    LOG.fine("Updating docker services");
                    try {
                        updateDockerAndNextflowServices(reg);
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Failed to update Docker services", ex);
                    }
                    // update the http services
                    LOG.fine("Updating http services for " + locations.size() + " locations");
                    try {
                        updateHttpServices(reg);
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Failed to update HTTP services", ex);
                    }
                    LOG.fine("Updating services complete: " + reg.fetchServiceDescriptorSets().size());
                });

    }

    protected void updateHttpServices(ServiceDescriptorRegistry reg) {
        locations.forEach((u, p) -> {
            LOG.info("Fetching HTTP services for " + u);
            // fetch it so that it exists
            ServiceDescriptorSet set = reg.fetchServiceDescriptorSet(u);
            Date now = new Date();
            int valid = 0;
            int invalid = 0;
            try {

                List<HttpServiceDescriptor> sds = ServiceDescriptorUtils.loadHttpServiceDescriptors(u);
                checkHealth(u, p, sds, now);

                set.setHealthUrl(p);
                set.updateServiceDescriptors(sds);
                for (ServiceDescriptor sd : set.getServiceDescriptors()) {
                    if (!sds.contains(sd)) {
                        sd.getServiceConfig().setStatus(ServiceConfig.Status.INACTIVE);
                        sd.getServiceConfig().setStatusLastChecked(now);
                        invalid++;
                    } else {
                        valid++;
                    }
                }
            } catch (Exception ex) {
                // failed so set status to inactive
                LOG.log(Level.WARNING, "Failed to update service descriptors for " + u, ex);
                set.getServiceDescriptors().forEach(sd -> {
                    sd.getServiceConfig().setStatus(ServiceConfig.Status.INACTIVE);
                    sd.getServiceConfig().setStatusLastChecked(now);
                });
                invalid = set.getServiceDescriptors().size();
            }
            // this saves the changes to the DB
            reg.updateServiceDescriptorSet(set);
            LOG.info("Updated HTTP services for " + u + ". " + valid + " valid and " + invalid + " invalid services defined");

        });
    }

    protected void updateDockerAndNextflowServices(ServiceDescriptorRegistry reg) throws IOException {

        Path root = FileSystems.getDefault().getPath(DOCKER_SERVICES_DIR);
        LOG.info("Looking for Docker service descriptors in " + root);
        Set<String> basePaths = new LinkedHashSet<>();
        Date now = new Date();

        // Docker and Nextflow service descriptors in JSON or YAML format
        Files.walk(root).forEach(p -> {
            String path = p.toString();
            String path_lc = path.toLowerCase();
            if (path_lc.endsWith(".json") || path_lc.endsWith(".yml") || path_lc.endsWith(".yaml")) {
                LOG.finer("Processing " + path);
                String relativePath = path.substring(root.toString().length());
                LOG.finer("Relative Path " + relativePath);

                Matcher m = EXECUTOR_PATTERN.matcher(relativePath);
                if (m.matches()) {
                    String base = m.group(1);
                    if (path_lc.endsWith(".dsd.json") || path_lc.endsWith(".dsd.yml") || path_lc.endsWith(".dsd.yaml")) {
                        readDockerServiceDescriptor(reg, p, base, basePaths, now);
                    } else if (path_lc.endsWith(".nsd.json") || path_lc.endsWith(".nsd.yml") || path_lc.endsWith(".nsd.yaml")) {
                        readNextflowServiceDescriptor(reg, p, base, basePaths, now);
                    } else {
                        LOG.info("Unrecognised file: " + path);
                    }
                } else {
                    LOG.info("Unable to parse " + relativePath);
                }
            }
        });

        // finally do the updates which persists the changes
        for (String base : basePaths) {
            LOG.fine("Updating services for " + base);
            int valid = 0;
            int invalid = 0;
            ServiceDescriptorSet set = reg.fetchServiceDescriptorSet(base);
            // if any services were not updated then they must have been removed so we inactive them.
            // we don't delete them otherwise old notebooks will blow up. We just don't allow them to be executed again.
            for (ServiceDescriptor sd : set.getServiceDescriptors()) {
                Date old = sd.getServiceConfig().getStatusLastChecked();
                if (old == null || !now.equals(old)) {
                    LOG.info("Inactivating service " + sd.getId() + " as no longer present");
                    sd.getServiceConfig().setStatus(ServiceConfig.Status.INACTIVE);
                    sd.getServiceConfig().setStatusLastChecked(now);
                    invalid++;
                } else {
                    valid++;
                }
            }
            reg.updateServiceDescriptorSet(set);
            LOG.info("Updated Docker services for " + base + ". " + valid + " valid and " + invalid + " invalid services defined");
        }
    }

    private void readDockerServiceDescriptor(ServiceDescriptorRegistry reg, Path p, String base, Set<String> basePaths, Date now) {
        String url = "file://docker-services/" + base;
        LOG.finer("URL: " + url);
        DockerServiceDescriptor sd = ServiceDescriptorUtils.readServiceDescriptor(p, DockerServiceDescriptor.class);
        handleServiceDescriptor(sd, p, reg, basePaths, now, url);
    }

    private void readNextflowServiceDescriptor(ServiceDescriptorRegistry reg, Path p, String base, Set<String> basePaths, Date now) {
        String url = "file://nextflow-services/" + base;
        LOG.finer("URL: " + url);
        NextflowServiceDescriptor sd = ServiceDescriptorUtils.readServiceDescriptor(p, NextflowServiceDescriptor.class);
        handleServiceDescriptor(sd, p, reg, basePaths, now, url);
    }


    private void handleServiceDescriptor(ServiceDescriptor sd, Path p, ServiceDescriptorRegistry reg, Set<String> basePaths, Date now, String url) {
        if (sd != null) {
            ServiceDescriptorSet set = reg.fetchServiceDescriptorSet(url);
            sd.getServiceConfig().setStatus(ServiceConfig.Status.ACTIVE);
            sd.getServiceConfig().setStatusLastChecked(now);
            set.updateServiceDescriptor(sd);
            if (!basePaths.contains(url)) {
                basePaths.add(url);
            }
            LOG.fine("Discovered Nextflow executor descriptor " + sd.getId() + " from file " + p);
        }
    }

    private ServiceDescriptorRegistry fetchDescriptorRegistry(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(ServiceConstants.KEY_SERVICE_REGISTRY, ServiceDescriptorRegistry.class);
    }

    private void checkHealth(String baseUrl, String healthUrl, List<HttpServiceDescriptor> serviceDescriptors, Date now) {

        String urlStr = healthUrl == null ? baseUrl : healthUrl;
        boolean b = true;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int code = connection.getResponseCode();
            b = code == 200;
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Health check failed for " + urlStr, ex);
        }

        ServiceConfig.Status status = (b ? ServiceConfig.Status.ACTIVE : ServiceConfig.Status.INACTIVE);
        LOG.fine("Setting status of " + serviceDescriptors.size() + " service descriptors from " + baseUrl + " with health URL of " + healthUrl + " to " + status);
        serviceDescriptors.forEach(sd -> {
            sd.getServiceConfig().setStatus(status);
            sd.getServiceConfig().setStatusLastChecked(now);
        });
    }

}
