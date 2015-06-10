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
        //camelContext.addComponent("activemq", activeMQComponent("vm://localhost?broker.persistent=false"));
        camelContext.addComponent("activemq", activeMQComponent("tcp://localhost:61616"));

        // add the routes
        camelContext.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("timer://foo?period=20&repeatCount=100")
                        .setBody(constant("Hello World!"))
                        .log("sending")
                        .to("activemq:queue:submit");

                from("activemq:queue:submit")
                        .log("received ${body}")
                        .delay(1000)
                        .to("activemq:queue:results");
            }
           
        });
        
        
        camelContext.start();
        Thread.sleep(200000);
        camelContext.stop();

    }

}
