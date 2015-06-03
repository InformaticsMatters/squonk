package com.im.lac.demo.routes;

import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class Jms {

    public static void main(String[] args) throws Exception {
        
        System.out.println("========================= JMSing =========================");

        final CamelContext camelContext = new DefaultCamelContext();

        // setup ActiveMQ
        camelContext.addComponent("jms", activeMQComponent("vm://localhost?broker.persistent=false"));

        // add the routes
        camelContext.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("timer://foo?period=1000")
                        .setBody(constant("Hello World!"))
                        .log("sending")
                        .to("jms:queue:incoming");

                from("jms:queue:incoming")
                        .log("received ${body}");
            }
           
        });
        
        
        camelContext.start();
        Thread.sleep(10000);
        camelContext.stop();

    }

}
