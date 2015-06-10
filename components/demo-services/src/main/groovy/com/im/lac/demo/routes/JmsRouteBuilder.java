package com.im.lac.demo.routes;

import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author timbo
 */
public class JmsRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer://foo?period=2000")
                .setBody(constant("Hello World!"))
                .log("sending")
                .to("activemq:queue:incoming");

        from("activemq:queue:incoming")
                .log("received ${body}");

    }

}
