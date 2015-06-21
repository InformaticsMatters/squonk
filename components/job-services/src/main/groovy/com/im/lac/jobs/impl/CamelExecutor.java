package com.im.lac.jobs.impl;

import com.im.lac.jobs.Job;
import com.im.lac.jobs.JobStatus;
import com.im.lac.service.DatasetService;
import com.im.lac.service.impl.SimpleJobStore;
import java.io.IOException;
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
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.toolbox.AggregationStrategies;

/**
 *
 * @author timbo
 */
public class CamelExecutor {
    
    private static final Logger LOG = Logger.getLogger(CamelExecutor.class.getName());
    
    public static final String DATASET_HANDLER = "DataSetHandler";
    public static final String JOB_STORE = "JobStore";
    public static final String ENDPOINT_SPLIT_AND_SUBMIT = "seda:splitAndSubmit";
    public static final String JMS_BROKER_NAME = "activemq";
    private final String brokerUri;
    
    final protected CamelContext camelContext;
    final private SimpleRegistry registry;
    final protected ProducerTemplate producerTemplate;
    private ExecutorService defaultExecutor;

    /**
     *
     * @param brokerUri Examples: vm://localhost?broker.persistent=false tcp://localhost:61616
     * @param datasetService
     */
    public CamelExecutor(String brokerUri, DatasetService datasetService) throws IOException {
        this.brokerUri = brokerUri;
        this.registry = new SimpleRegistry();
        registry.put(DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"));
        registry.put(JOB_STORE, new SimpleJobStore());
        
        this.camelContext = new DefaultCamelContext(registry);
        
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
    
    public JobStatus submitJob(Job job, String endpoint) {
        return getProducerTemplate().requestBody(endpoint, job, JobStatus.class);
    }
    
    public void start() throws Exception {
        
        LOG.log(Level.INFO, "Starting using {0} as JMS broker name with URI of {1}", new Object[]{JMS_BROKER_NAME, brokerUri});

        // setup ActiveMQ
        //camelContext.addComponent(JMS_BROKER_NAME, activeMQComponent("vm://localhost?broker.persistent=false"));
        camelContext.addComponent(JMS_BROKER_NAME, activeMQComponent(brokerUri));

        // add the routes
        camelContext.addRoutes(new AsyncJobRouteBuilder());
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
                
                from("seda:processDataset")
                        // input is the jobdef
                        .log("Processing ...")
                        .process((Exchange exchange) -> {
                            // 1. get the DataItem for the dataset ID
                            // 2. get the InputStream for the large object. contains json.
                        })
                        .to("seda:submit") // send for async execution
                        // job is now running
                        .process((Exchange exchange) -> {
                            // update status of the job 
                            // set JobStatus as the body
                        })
                        .marshal().json(JsonLibrary.Jackson) // body is now the JSON for the JobStatus
                        .log("Response sent");
                
                from("seda:submit")
                        // send  to the desired endpoint async
                        // when execution is complete results are send to seday:handleResults
                        .routingSlip(header("endpoint"));
                
                from("seda:handleResults")
                        .log("Handling results");
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
