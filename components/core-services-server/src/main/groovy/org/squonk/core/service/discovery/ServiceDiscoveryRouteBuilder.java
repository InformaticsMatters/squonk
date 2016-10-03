package org.squonk.core.service.discovery;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.squonk.core.ServerConstants;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptor.DataType;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author timbo
 */
public class ServiceDiscoveryRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(ServiceDiscoveryRouteBuilder.class.getName());

    public static final String ROUTE_REQUEST = "direct:request";
    private static final String DOCKER_IP = System.getenv("DOCKER_IP") != null ? System.getenv("DOCKER_IP") : "localhost";

    private final JsonHandler jsonHandler = new JsonHandler();

    /**
     * This allows the timer to be turned off or set to only run a certain
     * number of times, primarily to allow easy testing
     */
    protected int timerRepeats = 0;
    /**
     * This allows the timer delay to be set, primarily to allow easy testing
     */
    protected int timerDelay = 15 * 60 * 1000;

    final List<String> locations = new ArrayList<>();

    public ServiceDiscoveryRouteBuilder() {

        String basicChemServicesUrl = IOUtils.getConfiguration("SQUONK_BASIC_CHEM_SERVICES_URL", null);
        String rdkitPythonServicesUrl = IOUtils.getConfiguration("SQUONK_RDKIT_CHEM_SERVICES_URL", null);

        if (basicChemServicesUrl != null) {
            LOG.info("Enabling basic chem services from " + basicChemServicesUrl);
            locations.add(basicChemServicesUrl + "/chem-services-cdk-basic/rest/v1/calculators");
            locations.add(basicChemServicesUrl + "/chem-services-cdk-basic/rest/v1/converters");
            locations.add(basicChemServicesUrl + "/chem-services-chemaxon-basic/rest/v1/calculators");
            locations.add(basicChemServicesUrl + "/chem-services-chemaxon-basic/rest/v1/descriptors");
            locations.add(basicChemServicesUrl + "/chem-services-rdkit-basic/rest/v1/calculators");
            locations.add(basicChemServicesUrl + "/chem-services-rdkit-search/rest/v1/db");
            locations.add(basicChemServicesUrl + "/chem-services-openchemlib-basic/rest/v1/calculators");
            locations.add(basicChemServicesUrl + "/chem-services-smartcyp/rest/v1");
        } else {
            LOG.warning("Environment variable SQUONK_BASIC_CHEM_SERVICES_URL not defined. Basic Chem services will not be available");
        }

        if (rdkitPythonServicesUrl != null) {
            LOG.info("Enabling RDKit python services from " + rdkitPythonServicesUrl);
            //locations.add(rdkitPythonServicesUrl + "/rdkit_screen/");
            //locations.add(rdkitPythonServicesUrl + "/rdkit_cluster/");
            locations.add(rdkitPythonServicesUrl + "/rdkit_filter/");
            locations.add(rdkitPythonServicesUrl + "/rdkit_conf/");
        } else {
            LOG.warning("Environment variable SQUONK_RDKIT_CHEM_SERVICES_URL not defined. RDKit Python services will not be available");
        }
    }

    public ServiceDiscoveryRouteBuilder(Collection<String> locations) {
        this.locations.addAll(locations);
    }

    public static final ServiceDescriptor[] TEST_SERVICE_DESCRIPTORS = new ServiceDescriptor[]{
            new ServiceDescriptor(
                    "test.noop",
                    "NOOP Service",
                    "Does nothing other than submit a Job",
                    new String[]{"testing"},
                    "http://foo.com/something",
                    Object.class, // inputClass
                    Object.class, // outputClass
                    DataType.ITEM, // inputType
                    DataType.ITEM, // outputType
                    "default_icon.png",
                    "valueIsIgnored", // endpoint
                    true, // URL is relative
                    null,
                    null
            )
    };

    @Override
    public void configure() throws Exception {

        from(ROUTE_REQUEST)
                .log("ROUTE_REQUEST")
                .process((Exchange exch) -> {
                    List<ServiceDescriptor> list = new ArrayList<>();
                    //list.addAll(Arrays.asList(TEST_SERVICE_DESCRIPTORS));
                    list.addAll(getServiceDescriptorStore(exch.getContext()).getServiceDescriptors());
                    exch.getIn().setBody(list);
                });

        // This updates the currently available services on a scheduled basis
        from("timer:discover?period=" + timerDelay + "&repeatCount=" + timerRepeats)
                .process((Exchange exch) -> exch.getIn().setBody(locations))
                .split(body())
                .log(LoggingLevel.INFO, "Discovering services for ${body}")
                .setHeader(Exchange.HTTP_URI, simple("${body}"))
                .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
                .to("http4:foo.bar/?throwExceptionOnFailure=false")
                .choice()
                .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200)) // we got a valid response
                .process((Exchange exch) -> {
                    ServiceDescriptorStore store = getServiceDescriptorStore(exch.getContext());
                    String url = exch.getIn().getHeader(Exchange.HTTP_URI, String.class);
                    String json = exch.getIn().getBody(String.class);
                    if (json != null) {
                        Iterator<ServiceDescriptor> iter = jsonHandler.iteratorFromJson(json, ServiceDescriptor.class);
                        while (iter.hasNext()) {
                            ServiceDescriptor sd = iter.next();
                            LOG.info("Adding service descriptor " + sd.getId());
                            store.addServiceDescriptor(url, ServiceDescriptorUtils.makeAbsolute(url, sd));
                        }
                    }
                })
                .log(LoggingLevel.DEBUG, "Site ${header[" + Exchange.HTTP_URI + "]} updated.")
                .endChoice()
                .otherwise() // anything else and we remove the service descriptor definitions from the store
                .log(LoggingLevel.INFO, "Site ${header[" + Exchange.HTTP_URI + "]} not responding. Removing from available services.")
                .process((Exchange exch) -> {
                    ServiceDescriptorStore store = getServiceDescriptorStore(exch.getContext());
                    String url = exch.getIn().getHeader(Exchange.HTTP_URI, String.class);
                    store.removeServiceDescriptors(url);
                })
                .end();

    }

    final ServiceDescriptorStore getServiceDescriptorStore(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(ServerConstants.SERVICE_DESCRIPTOR_STORE, ServiceDescriptorStore.class);
    }

}
