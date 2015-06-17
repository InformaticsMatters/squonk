package com.im.lac.jobs.run;

import java.util.concurrent.Future;
import java.util.logging.Logger;
import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class TestAsyncRequestBodyForJMS {
    
    private static final Logger LOG = Logger.getLogger(TestAsyncRequestBodyForJMS.class.getName());
    
    public static void main(String[] args) throws Exception {
        
        System.out.println("========================= Setup =========================");
        
        final CamelContext camelContext = new DefaultCamelContext();
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        // setup ActiveMQ
        camelContext.addComponent("activemq", activeMQComponent("vm://localhost?broker.persistent=false"));
        //camelContext.addComponent("activemq", activeMQComponent("tcp://localhost:61616"));

        // add the routes
        camelContext.addRoutes(new RouteBuilder() {
            
            @Override
            public void configure() throws Exception {
                
                
                from("activemq:queue:myqueue")
                        .log("bananaing")
                        .delay(1000)
                        .transform(constant("banana"));
                
            }
            
        });
        
        camelContext.start();


        Future<String> future = producerTemplate.asyncRequestBody("activemq:queue:myqueue", "foo", String.class);
        LOG.info("sending complete");
        LOG.info("result " + future.get());
        
        Thread.sleep(2000);
        camelContext.stop();
        
    }
    
}
