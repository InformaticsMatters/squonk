package com.im.lac.jobs.run;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class Test1 {

    public static void main(String[] args) throws Exception {

        System.out.println("========================= Setup =========================");

        final CamelContext camelContext = new DefaultCamelContext();
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        // setup ActiveMQ
        //camelContext.addComponent("activemq", activeMQComponent("vm://localhost?broker.persistent=false"));
        camelContext.addComponent("activemq", activeMQComponent("tcp://localhost:61616"));

        // add the routes
        camelContext.addRoutes(new RouteBuilder() {
            
            

            @Override
            public void configure() throws Exception {

                from("activemq:queue:fruit")
                        .log("received fruit ${body}, result will be sent to ${header.JMSReplyTo}");

                from("activemq:queue:bananas?disableReplyTo=true")
                        .log("received some bananas ${body}");

                from("direct:splitAndSubmit")
                        .log("Will submit to queue ${header.CamelJmsDestinationName}, result will be sent to ${header.JMSReplyTo}")
                        .split(body())
                        .log("Submitting ${body}")
                        .to(ExchangePattern.InOnly, "activemq:queue:dummy?preserveMessageQos=true");
            }

        });

        camelContext.start();
        Map headers = new HashMap();
        headers.put("CamelJmsDestinationName", "fruit");
        headers.put("JMSReplyTo", "bananas");

        producerTemplate.sendBodyAndHeaders(
                "direct:splitAndSubmit",
                Arrays.asList(new String[]{"banana", "pineapple", "orange", "apple", "pear"}),
                headers);

        Thread.sleep(10000);
        camelContext.stop();

    }

}
