package com.im.lac.jobs.impl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.toolbox.AggregationStrategies;

/**
 *
 * @author timbo
 */
public class CamelExecutor {

    private static final Logger LOG = Logger.getLogger(CamelExecutor.class.getName());

    public static final String ENDPOINT_SPLIT_AND_SUBMIT = "seda:splitAndSubmit";
    public static final String JMS_BROKER_NAME = "activemq";
    private final String brokerUri;

    final protected CamelContext camelContext;
    final protected ProducerTemplate producerTemplate;
    private ExecutorService defaultExecutor;

    /**
     *
     * @param brokerUri Examples: vm://localhost?broker.persistent=false tcp://localhost:61616
     */
    public CamelExecutor(String brokerUri) {
        this.brokerUri = brokerUri;
        camelContext = new DefaultCamelContext();
        producerTemplate = camelContext.createProducerTemplate();
    }

    public ProducerTemplate getProducerTemplate() {
        return producerTemplate;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public <T> Future<T> submitTask(Callable<T> task) {
        return defaultExecutor.submit(task);
    }

    public Future<?> submitTask(Runnable task) {
        return defaultExecutor.submit(task);
    }

    public <T> Future<T> submitTask(Runnable task, T result) {
        return defaultExecutor.submit(task, result);
    }

    public void start() throws Exception {

        LOG.log(Level.INFO, "Starting using {0} as JMS broker name with URI of {1}", new Object[]{JMS_BROKER_NAME, brokerUri});

        // setup ActiveMQ
        //camelContext.addComponent(JMS_BROKER_NAME, activeMQComponent("vm://localhost?broker.persistent=false"));
        camelContext.addComponent(JMS_BROKER_NAME, activeMQComponent(brokerUri));

        // add the routes
        camelContext.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                // splits the body and sends each item to the JMS queue specified by the CamelJmsDestinationName header
                // returns the number of items split/posted as the body
                from(ENDPOINT_SPLIT_AND_SUBMIT)
                        .log("Will submit ${body} to queue ${header.CamelJmsDestinationName}, result will be sent to ${header.JMSReplyTo}")
                        .split(body(), AggregationStrategies.useLatest()).streaming()
                        //.log("Submitting ${body}")
                        .to(ExchangePattern.InOnly, "activemq:queue:dummy?preserveMessageQos=true")
                        .end()
                        .setBody(header("CamelSplitSize"))
                        .log("Split and sent ${body} items");

                // simple test route
                from("direct:helloToStream")
                        .process((Exchange exchange) -> {
                            List<String> data = exchange.getIn().getBody(List.class);
                            List<String> result = data.stream()
                            .map(s -> "Hello " + s)
                            .collect(Collectors.toList());
                            exchange.getIn().setBody(result);
                        });
                
                from("direct:helloToString")
                        .transform(simple("Hello ${body}"));
            }

        });

        camelContext.start();

        defaultExecutor = camelContext.getExecutorServiceManager().newThreadPool(this, "DefaultJobPool", 5, 25);
    }

    public void stop() throws Exception {
        LOG.info("stopping");
        camelContext.getExecutorServiceManager().shutdownGraceful(defaultExecutor);
        camelContext.stop();
    }

}
