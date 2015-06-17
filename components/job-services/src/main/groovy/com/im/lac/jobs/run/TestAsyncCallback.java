package com.im.lac.jobs.run;

import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Synchronization;

public class TestAsyncCallback {

    private static final Logger LOG = Logger.getLogger(TestAsyncCallback.class.getName());

    public static void main(String[] args) throws Exception {

        System.out.println("========================= Setup =========================");

        final CamelContext camelContext = new DefaultCamelContext();
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        // add the routes
        camelContext.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                from("direct:another")
                        .transform(constant("bar"));
            }
        });

        camelContext.start();

        producerTemplate.asyncCallback(
                "direct:another",
                new Processor() {
                    @Override
                    public void process(Exchange exchange) {
                        exchange.getIn().setBody("pineapple");
                    }
                },
                new Synchronization() {

                    @Override
                    public void onComplete(Exchange exchng) {
                        Object inBody = exchng.getIn().getBody();
                        System.out.println("onComplete:  " + inBody);
                    }

                    @Override
                    public void onFailure(Exchange exchng) {
                        System.out.println("BANG");
                    }
                }
        );
        Thread.sleep(2000);
        camelContext.stop();

    }

}
