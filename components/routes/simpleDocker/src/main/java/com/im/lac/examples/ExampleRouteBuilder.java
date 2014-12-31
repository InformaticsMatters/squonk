package com.im.lac.examples;

import java.util.List;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 * Example Camel service that provides access to a simple route both directly
 * (within the same Camel context) and as a simple REST web service
 *
 * @author timbo
 */
public class ExampleRouteBuilder extends RouteBuilder {

    public static final Integer DEFAULT_PORT = 8080;
    public final String host;
    public final Integer port;

    public ExampleRouteBuilder(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public ExampleRouteBuilder(Integer port) {
        this("localhost", DEFAULT_PORT);
    }

    public ExampleRouteBuilder() {
        this("localhost", DEFAULT_PORT);
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("restlet").host(host).port(port).bindingMode(RestBindingMode.off);

        // simples example
        rest("/ping").get()
                .route().transform().constant("Service Running");
        
        // example of how to get request params
        rest("/try/{id}").get()
                .route().transform().simple("Trying ${header[id]}");

        // This receives a POST request, processes it and returns the result
        rest("/convert").post()
                .to("direct:tolowercase");

        // This handles a POST request using JSON
        rest("/statistics")
                .post().bindingMode(RestBindingMode.json).type(List.class)
                .to("direct:stats");

        // this is where the action happens
        from("direct:tolowercase")
                .log(LoggingLevel.DEBUG, "Got something: ${body}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) {
                        String body = exchange.getIn().getBody(String.class);
                        exchange.getIn().setBody(body.toLowerCase());
                    }
                })
                .log(LoggingLevel.DEBUG, "Converted to : ${body}");

        from("direct:stats")
                .log(LoggingLevel.DEBUG, "Got values: ${body}")
                .process(new SumProcessor());
    }
}
