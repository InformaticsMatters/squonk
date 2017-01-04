package org.squonk.core.service.discovery;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.execution.docker.DescriptorRegistry;
import org.squonk.execution.docker.DockerExecutorDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.squonk.core.CommonConstants.KEY_DOCKER_SERVICE_REGISTRY;

/**
 * @author timbo
 */
public class ServiceDiscoveryRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ServiceDiscoveryRouteBuilder.class.getName());

    public static final String ROUTE_REQUEST = "direct:request";

    private final JsonHandler jsonHandler = JsonHandler.getInstance();
    private final PostgresServiceDescriptorClient client = new PostgresServiceDescriptorClient();

    /**
     * This allows the timer to be turned off or set to only run a certain
     * number of times, primarily to allow easy testing
     */
    protected int timerRepeats = 0;

    /**
     * This allows the timer delay to be set, primarily to allow easy testing
     */
    protected int timerDelay = 15 * 60 * 1000;

    private final Map<String, String> locations = new LinkedHashMap<>();

    public ServiceDiscoveryRouteBuilder() {

        String basicChemServicesUrl = IOUtils.getConfiguration("SQUONK_BASIC_CHEM_SERVICES_URL", null);
        String rdkitPythonServicesUrl = IOUtils.getConfiguration("SQUONK_RDKIT_CHEM_SERVICES_URL", null);

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
            locations.put(basicChemServicesUrl + "/chem-services-cpsign/rest/v1", basicChemServicesUrl + "/chem-services-cpsign/rest/ping");

        } else {
            LOG.warning("Environment variable SQUONK_BASIC_CHEM_SERVICES_URL not defined. Basic Chem services will not be available");
        }

//        if (rdkitPythonServicesUrl != null) {
//            LOG.info("Enabling RDKit python services from " + rdkitPythonServicesUrl);
//
//            locations.put(rdkitPythonServicesUrl + "/rdkit_screen/", null);
//            locations.put(rdkitPythonServicesUrl + "/rdkit_cluster/", null);
//            locations.put(rdkitPythonServicesUrl + "/rdkit_filter/", null);
//            locations.put(rdkitPythonServicesUrl + "/rdkit_conf/", null);
//        } else {
//            LOG.warning("Environment variable SQUONK_RDKIT_CHEM_SERVICES_URL not defined. RDKit Python services will not be available");
//        }
    }

    public static final ServiceDescriptor[] TEST_SERVICE_DESCRIPTORS = new ServiceDescriptor[]{
            new ServiceDescriptor(
                    "test.noop",
                    "NOOP Service",
                    "Does nothing other than submit a Job",
                    new String[]{"testing"},
                    "http://foo.com/something",
                    "default_icon.png",
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
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
                    List<ServiceDescriptorSet> sets = client.list();
                    List<ServiceDescriptor> list = new ArrayList<>();
                    for (ServiceDescriptorSet set : sets) {
                        list.addAll(set.getServiceDescriptors());
                    }
                    List<ServiceDescriptor> dockerServiceDescriptors = getDockerServiceDescriptors(exch.getContext());
                    list.addAll(dockerServiceDescriptors);
                    LOG.info("Added " + dockerServiceDescriptors.size() + " docker service descriptors");
                    exch.getIn().setBody(list);
                });

        // This updates the currently available services on a scheduled basis
        from("timer:discover?period=" + timerDelay + "&repeatCount=" + timerRepeats)

                .process((Exchange exch) -> {
                    locations.forEach((u, p) -> {
                        try {
                            List<ServiceDescriptor> sds = loadServiceDescriptors(u);
                            checkHealth(u, p, sds);
                            updateServiceDescriptors(u, p, sds);
                        } catch (Exception ex1) {
                            LOG.warning("Failed to update service descriptors for " + u + " cause " + ex1.getMessage());
                            try {
                                client.updateServiceDescriptorStatus(u, ServiceDescriptor.Status.INACTIVE, new Date());
                            } catch (Exception ex2) {
                                LOG.warning("Failed to update status of service descriptors for " + u + " cause " + ex2.getMessage());
                            }
                        }
                    });
                });

    }

    private List<ServiceDescriptor> getDockerServiceDescriptors(CamelContext context) {
        DescriptorRegistry reg = context.getRegistry().lookupByNameAndType(KEY_DOCKER_SERVICE_REGISTRY, DescriptorRegistry.class);
        Map<String,DockerExecutorDescriptor> items = reg.fetchAll();
        List<ServiceDescriptor> list = new ArrayList<>(items.size());
        for (Map.Entry<String,DockerExecutorDescriptor> e : items.entrySet()) {
            list.add(e.getValue().getServiceDescriptor());
        }
        return list;
    }

    protected void updateServiceDescriptors(String baseUrl, String healthUrl, List<ServiceDescriptor> sds) throws Exception {
        client.update(new ServiceDescriptorSet(baseUrl, healthUrl, sds));
    }

    private void checkHealth(String baseUrl, String healthUrl, List<ServiceDescriptor> serviceDescriptors) {
        boolean b = true;
        final Date now = new Date();
        if (healthUrl != null) {
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
        }
        ServiceDescriptor.Status status = (b? ServiceDescriptor.Status.ACTIVE : ServiceDescriptor.Status.INACTIVE);
        LOG.info("Setting status of " + serviceDescriptors.size() + " service descriptors from " + baseUrl + " with health URL of " + healthUrl + " to " + status);
        serviceDescriptors.forEach(sd -> {
            sd.setStatus(status);
            sd.setStatusLastChecked(now);
        });
    }

    private List<ServiceDescriptor> loadServiceDescriptors(String url) throws IOException {
        ObjectReader reader = jsonHandler.getObjectMapper().readerFor(ServiceDescriptor.class);
        MappingIterator<ServiceDescriptor> iter = reader.readValues(new URL(url));
        List<ServiceDescriptor> list = new ArrayList<>();
        while (iter.hasNext()) {
            ServiceDescriptor absUrlSD = ServiceDescriptorUtils.makeAbsolute(url, iter.next());
            list.add(absUrlSD);
        }
        return list;
    }

}
