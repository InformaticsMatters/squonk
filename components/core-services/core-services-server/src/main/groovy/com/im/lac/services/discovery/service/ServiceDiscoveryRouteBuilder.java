package com.im.lac.services.discovery.service;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncLocalProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.DoNothingJobDefinition;
import com.im.lac.services.AccessMode;
import com.im.lac.services.ServerConstants;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.job.service.AsyncJobRouteBuilder;
import com.im.lac.types.io.JsonHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author timbo
 */
public class ServiceDiscoveryRouteBuilder extends RouteBuilder {

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
    protected int timerDelay = 60000;

    List<String> locations = Arrays.asList(new String[]{
        "http://squonk-javachemservices.elasticbeanstalk.com/chem-services-chemaxon-basic/rest/v1/calculators",
        "http://squonk-javachemservices.elasticbeanstalk.com/chem-services-chemaxon-basic/rest/v1/descriptors",
        "http://squonk-javachemservices.elasticbeanstalk.com/chem-services-cdk-basic/rest/v1/calculators",
        "http://squonk-dockerchemservices.elasticbeanstalk.com/rdkit_cluster",
        "http://squonk-dockerchemservices.elasticbeanstalk.com/rdkit_screen"
    });

    public static final ServiceDescriptor[] TEST_SERVICE_DESCRIPTORS = new ServiceDescriptor[]{
        new ServiceDescriptor(
        "test.noop",
        "NOOP Service",
        "Does nothing other than create a Job",
        new String[]{"testing"},
        null,
        new String[]{"/Testing"},
        "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
        null,
        new String[]{"testing"},
        Object.class, // inputClass
        Object.class, // outputClass
        Metadata.Type.ITEM, // inputType
        Metadata.Type.ITEM, // outputType
        new AccessMode[]{
            new AccessMode(
            "donothing",
            "Immediate execution",
            "Execute as an asynchronous REST web service",
            "valueIsIgnored", // endpoint
            false, // URL is relative
            DoNothingJobDefinition.class,
            null,
            null,
            null,
            null,
            null,
            null)
        }
        ),
//        new ServiceDescriptor(
//        "test.echo.http",
//        "Echo Service (HTTP)",
//        "Reads a dataset and writes it back as a new dataset",
//        new String[]{"testing"
//        },
//        null,
//        new String[]{"/Testing"
//        },
//        "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
//        null,
//        new String[]{"testing"},
//        Object.class, // inputClass
//        Object.class, // outputClass
//        Metadata.Type.ARRAY, // inputType
//        Metadata.Type.ARRAY, // outputType
//        new AccessMode[]{
//            new AccessMode(
//            "asyncHttp",
//            "Immediate execution",
//            "Execute as an asynchronous REST web service",
//            "http://" + DOCKER_IP + "/coreservices/rest/echo", // the  endpoint
//            false, // URL is relative
//            AsyncHttpProcessDatasetJobDefinition.class,
//            null,
//            null,
//            null,
//            null,
//            null)
//        }
//        ),
        new ServiceDescriptor(
        "test.echo.local",
        "Echo Service (local)",
        "Reads a dataset and writes it back as a new dataset",
        new String[]{"testing"
        },
        null,
        new String[]{"/Testing"
        },
        "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
        null,
        new String[]{"testing"},
        Object.class, // inputClass
        Object.class, // outputClass
        Metadata.Type.ARRAY, // inputType
        Metadata.Type.ARRAY, // outputType
        new AccessMode[]{
            new AccessMode(
            "asyncLocal",
            "Immediate execution",
            "Execute as an asynchronous REST web service",
            AsyncJobRouteBuilder.ROUTE_DUMMY, // the direct:simpleroute endpoint
            false, // URL is relative
            AsyncLocalProcessDatasetJobDefinition.class,
            null,
            null,
            null,
            null,
            null,
            null)
        }
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
                .log(LoggingLevel.DEBUG, "Discovering services for ${body}")
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
                            store.addServiceDescriptor(url, ServiceDescriptorUtils.makeAbsolute(url, sd));
                        }
                    }
                })
                .log(LoggingLevel.DEBUG, "Site ${header[" + Exchange.HTTP_URI + "]} updated.")
                .endChoice()
                .otherwise() // anything else and we remove the service descriptor defintions from the store
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
