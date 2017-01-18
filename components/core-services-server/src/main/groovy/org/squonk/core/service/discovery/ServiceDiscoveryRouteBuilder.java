package org.squonk.core.service.discovery;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.squonk.core.*;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Stream;

import static org.squonk.core.CommonConstants.KEY_SERVICE_REGISTRY;

/**
 * @author timbo
 */
public class ServiceDiscoveryRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ServiceDiscoveryRouteBuilder.class.getName());

    public static final String ROUTE_REQUEST = "direct:request";
    private static Pattern DOCKER_EXECUTOR_PATTERN = Pattern.compile("/(\\w+)/(.*)");
    protected String DOCKER_SERVICES_DIR = IOUtils.getConfiguration("SQUONK_DOCKER_SERVICES_DIR", "../../data/testfiles/docker-services");

    private final JsonHandler jsonHandler = JsonHandler.getInstance();

    /**
     * This allows the timer to be turned off or set to only run a certain
     * number of times, primarily to allow easy testing
     */
    protected int timerRepeats = 0;

    /**
     * This allows the timer delay to be set, primarily to allow easy testing
     */
    protected int timerDelay = 15 * 60 * 1000;
    //protected int timerDelay = 1 * 60 * 1000;

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
                    reg.init(); // this ensures the descriptors are loaded from the DB before we update anything
                    // update the docker services
                    LOG.fine("Updating docker services");
                    try {
                        updateDockerServices(reg);
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
            LOG.fine("Fetching service descriptors for " + u);
            // fetch it so that it exists
            ServiceDescriptorSet set = reg.fetchServiceDescriptorSet(u);
            Date now = new Date();
            int valid = 0;
            int invalid = 0;
            try {

                List<HttpServiceDescriptor> sds = loadHttpServiceDescriptors(u);
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

    protected void updateDockerServices(ServiceDescriptorRegistry reg) throws IOException {

        Path root = FileSystems.getDefault().getPath(DOCKER_SERVICES_DIR);
        LOG.fine("Looking for Docker descriptors in " + root);
        Stream<Path> paths = Files.walk(root);
        Set<String> basePaths = new LinkedHashSet<>();
        Date now = new Date();
        paths.filter(p -> p.toString().endsWith(".dsd"))
                .forEach(p -> {
                    LOG.finer("Processing " + p);
                    String relativePath = p.toString().substring(root.toString().length());
                    LOG.finer("Relative Path " + relativePath);

                    Matcher m = DOCKER_EXECUTOR_PATTERN.matcher(relativePath);
                    if (m.matches()) {
                        String base = m.group(1);
                        String url = "file://docker-services/" + base;
                        LOG.finer("URL: " + url);

                        try (InputStream is = new FileInputStream(p.toFile())) {
                            DockerServiceDescriptor dsd = JsonHandler.getInstance().objectFromJson(is, DockerServiceDescriptor.class);
                            ServiceDescriptorSet set = reg.fetchServiceDescriptorSet(url);
                            dsd.getServiceConfig().setStatus(ServiceConfig.Status.ACTIVE);
                            dsd.getServiceConfig().setStatusLastChecked(now);
                            set.updateServiceDescriptor(dsd);
                            if (!basePaths.contains(url)) {
                                basePaths.add(url);
                            }
                            LOG.info("Discovered docker executor descriptor " + dsd.getId() + " from file " + p);
                        } catch (IOException ex) {
                            LOG.log(Level.INFO, "Unable to read descriptor for " + p, ex);
                        }
                    } else {
                        LOG.info("Unable to parse " + relativePath);
                    }
                });

        // finally do the updates which persists the changes
        int valid = 0;
        int invalid = 0;
        for (String base : basePaths) {
            LOG.fine("Updating Docker services for " + base);
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


    private ServiceDescriptorRegistry fetchDescriptorRegistry(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(KEY_SERVICE_REGISTRY, ServiceDescriptorRegistry.class);
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

    private List<HttpServiceDescriptor> loadHttpServiceDescriptors(String url) throws IOException {
        ObjectReader reader = jsonHandler.getObjectMapper().readerFor(HttpServiceDescriptor.class);
        MappingIterator<HttpServiceDescriptor> iter = reader.readValues(new URL(url));
        List<HttpServiceDescriptor> list = new ArrayList<>();
        while (iter.hasNext()) {
            HttpServiceDescriptor sd = iter.next();
            HttpServiceDescriptor absUrlSD = ServiceDescriptorUtils.makeAbsolute(url, sd);
            list.add(absUrlSD);
        }
        return list;
    }

}
