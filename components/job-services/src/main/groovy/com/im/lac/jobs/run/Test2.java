package com.im.lac.jobs.run;

import java.util.concurrent.Future;
import java.util.logging.Logger;
import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class Test2 {
    
    private static final Logger LOG = Logger.getLogger(Test2.class.getName());
    
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
                
                from("direct:start")
                        .log("body 1 ${body}")
                        .to("direct:another")
                        .log("body 2 ${body}")
                        .to("activemq:queue:myqueue")
                        .log("body 3 ${body}");
                
                from("direct:another")
                        .transform(constant("bar"));
                
                from("seda:sender")
                        .log("sending ${body} to AMQ")
                        .to(ExchangePattern.InOut, "activemq:queue:dummy")
                        .log("got back ${body}");
                
                from("activemq:queue:myqueue")
                        .log("bananaing")
                        .delay(5000)
                        .transform(constant("banana"));
                
            }
            
        });
        
        camelContext.start();

        //producerTemplate.sendBodyAndHeader("seda:sender", "foo", "CamelJmsDestinationName", "myqueue");
        //LOG.info("sending complete");
        Future<String> future = producerTemplate.asyncRequestBody("activemq:queue:myqueue", "foo", String.class);
        LOG.info("sending complete");
//        , new Synchronization() {
//            
//            @Override
//            public void onComplete(Exchange exchng) {
//                LOG.info("onCompelete " + exchng.getIn().getBody());
//            }
//            
//            @Override
//            public void onFailure(Exchange exchng) {
//                LOG.warning("BANG!");
//            }
//        });
        LOG.info("result " + future.get());
        

//        producerTemplate.asyncCallback(
//                //"activemq:queue:myqueue?asyncConsumer=true",
//               "direct:another",
//                new Processor() {
//                    @Override
//                    public void process(Exchange exchange) {
//                        exchange.getIn().setBody("pineapple");
//                    }
//                },
//                new Synchronization() {
//
//                    @Override
//                    public void onComplete(Exchange exchng) {
//                        Object inBody = exchng.getIn().getBody();
//                        System.out.println("onComplete:  " + inBody);
//                    }
//
//                    @Override
//                    public void onFailure(Exchange exchng) {
//                        System.out.println("BANG");
//                    }
//                }
//        );
        Thread.sleep(2000);
        camelContext.stop();
        
    }
    
}
