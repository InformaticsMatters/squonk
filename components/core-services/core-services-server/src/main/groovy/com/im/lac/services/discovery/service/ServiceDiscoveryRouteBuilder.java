package com.im.lac.services.discovery.service;

import com.im.lac.services.ServiceDescriptorSet;
import com.im.lac.services.ServiceDescriptor;
import com.im.lac.types.io.JsonHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author timbo
 */
public class ServiceDiscoveryRouteBuilder extends RouteBuilder {

    public static final String ROUTE_REQUEST = "direct:request";

    private final JsonHandler jsonHandler = new JsonHandler();

    /**
     * This allows the timer to be turned off or set to only run a certain number of times,
     * primarily to allow easy testing
     */
    protected int timerRepeats = 0;
    /**
     * This allows the timer delay to be set, primarily to allow easy testing
     */
    protected int timerDelay = 60000;

    List<String> locations = Arrays.asList(new String[]{
        "http://squonk-javachemservices.elasticbeanstalk.com/chem-services-chemaxon-basic/rest/v1/calculators",
        "http://squonk-javachemservices.elasticbeanstalk.com/chem-services-chemaxon-basic/rest/v1/descriptors",
        "http://squonk-javachemservices.elasticbeanstalk.com/chem-services-cdk-basic/rest/v1/calculators"
    });

    protected Map<String, ServiceDescriptorSet> serviceDefintions = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void configure() throws Exception {
        
        

        from(ROUTE_REQUEST)
                .log("ROUTE_REQUEST")
                .process((Exchange exch) -> exch.getIn().setBody(serviceDefintions.values()));

        // This updates the currently available services on a scheduled basis
        from("timer:discover?period=" + timerDelay + "&repeatCount=" + timerRepeats)
                .process((Exchange exch) -> exch.getIn().setBody(locations))
                .split(body())
                .log(LoggingLevel.DEBUG, "Discovering services for ${body}")
                .setHeader(Exchange.HTTP_URI, simple("${body}"))
                .setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
                .to("http4:foo.bar/?throwExceptionOnFailure=false")
                .choice()
                .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(200))
                .process((Exchange exch) -> {
                    String url = exch.getIn().getHeader(Exchange.HTTP_URI, String.class);
                    String json = exch.getIn().getBody(String.class);
                    if (json != null) {
                        Iterator<ServiceDescriptor> iter = jsonHandler.iteratorFromJson(json, ServiceDescriptor.class);
                        List<ServiceDescriptor> list = new ArrayList<>();
                        while (iter.hasNext()) {
                            list.add(iter.next());
                        }
                        ServiceDescriptorSet defn = new ServiceDescriptorSet(url, list.toArray(new ServiceDescriptor[list.size()]));
                        serviceDefintions.put(url, defn);
                    }
                })
                .log(LoggingLevel.DEBUG, "Site ${header[" + Exchange.HTTP_URI + "]} updated.")
                .endChoice()
                .otherwise()
                .log(LoggingLevel.INFO, "Site ${header[" + Exchange.HTTP_URI + "]} not responding. Removing from available services.")
                .process((Exchange exch) -> {
                    String url = exch.getIn().getHeader(Exchange.HTTP_URI, String.class);
                    serviceDefintions.remove(url);
                })
                .end();

    }

}
