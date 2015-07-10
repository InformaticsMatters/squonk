package com.im.lac.services.job.service;

import com.im.lac.services.ServerConstants;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.SplitAndQueueProcessDatasetJobDefinition;
import com.im.lac.services.CommonConstants;
import static com.im.lac.services.job.service.JobServiceRouteBuilder.ROUTE_SUBMIT_PREFIX;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.toolbox.AggregationStrategies;

public class SplitAndQueueJobRouteBuilder extends RouteBuilder {

    private String mqVirtualHost = null;
    private String mqUsername = null;
    private String mqPassword = null;
    

    private static final Logger LOG = Logger.getLogger(SplitAndQueueJobRouteBuilder.class.getName());

    public static final String ROUTE_SPLIT_AND_QUEUE_SUBMIT = ROUTE_SUBMIT_PREFIX + SplitAndQueueProcessDatasetJobDefinition.class.getName();
    public static final String ROUTE_FETCH_AND_DISPATCH = "seda:queueProcessDatasetSubmit";
    public static final String ENDPOINT_SPLIT_AND_SUBMIT = "seda:splitAndSubmit";
    public static final String DUMMY_MESSAGE_QUEUE = "rabbitmq";
    
    
    SplitAndQueueJobRouteBuilder() {
        
    }
    
    
   SplitAndQueueJobRouteBuilder(String mqVirtualHost, String mqUsername, String mqPassword) {
        this.mqVirtualHost = mqVirtualHost;
        this.mqUsername = mqUsername;
        this.mqPassword = mqPassword;
    }
    
    @Override
    public void configure() throws Exception {

        MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials(null, mqUsername, mqPassword, mqVirtualHost, null);
        String mqueueUrl = getRabbitMQUrl(rabbitmqCredentials);
        LOG.log(Level.INFO, "Using RabbitMQ URL of {0}", mqueueUrl);

        // This is the entrypoint. Send your AbstractDatasetJob here.
        // Sends the job for execution and returns immediately with the Job status updated accordingly
        from(ROUTE_SPLIT_AND_QUEUE_SUBMIT)
                // body is the jobdef
                .log("ROUTE_SPLIT_AND_QUEUE_SUBMIT Processing dataset using queue submit ... ${body}")
                .setHeader(ServerConstants.HEADER_DATASET_ID, simple("${body.datasetId}"))
                // create the job
                .process((Exchange exch) -> {
                    SplitAndQueueProcessDatasetJobDefinition jobdef = exch.getIn().getBody(SplitAndQueueProcessDatasetJobDefinition.class);
                    SplitAndQueueJob job = new SplitAndQueueJob(jobdef);
                    job.status = JobStatus.Status.PENDING;
                    JobHandler.getJobStore(exch).putJob(job);
                    exch.getIn().setBody(job);
                    exch.getIn().setHeader(CommonConstants.HEADER_JOB_ID, job.getJobId());
                })
                // send for execution
                .to(ExchangePattern.InOnly, ROUTE_FETCH_AND_DISPATCH)
                // job is now running
                .process((Exchange exch) -> {
                    // set the updated status as the body
                    AbstractDatasetJob job = exch.getIn().getBody(AbstractDatasetJob.class);
                    job.status = JobStatus.Status.PENDING;
                    JobHandler.getJobStore(exch).putJob(job);
                    exch.getIn().setBody(job.buildStatus());
                    exch.getIn().setHeader(CommonConstants.HEADER_JOB_ID, job.getJobId());
                })
                // body is now JobStatus
                .log("Queue submit complete");

        // This is where most of the work is done. The dataset is retreived and then forwarded to the
        // specified endpoint. the endpoint returns the results and these are then forwarded on to be saved. 
        from(ROUTE_FETCH_AND_DISPATCH)
                .log("ROUTE_FETCH_AND_DISPATCH")
                // body is the job
                .setExchangePattern(ExchangePattern.InOut)
                // fetch the dataset to process
                .process((Exchange exch) -> JobHandler.setBodyAsObjectsForDataset(exch))
                // body is now the dataset
                // send be split and submitted
                .to(ENDPOINT_SPLIT_AND_SUBMIT)
                .process((Exchange exch) -> JobHandler.putCurrentJobStatus(exch))
                .log("Job is being processed");

        // splits the body and sends each item to the JMS queue specified by the CamelJmsDestinationName header
        // returns the number of items split/posted as the body
        from(ENDPOINT_SPLIT_AND_SUBMIT)
                .log("ENDPOINT_SPLIT_AND_SUBMIT Splitting and submitting ${body}")
                .process((Exchange exch) -> {
                    SplitAndQueueJob job = JobHandler.getJob(exch, SplitAndQueueJob.class);
                    JobHandler.setJobStatus(exch, JobStatus.Status.SUBMITTING);
                    LOG.log(Level.INFO, "Setting ROUTING_KEY to {0}", job.getJobDefinition().getQueuename());
                    LOG.log(Level.INFO, "Setting REPLY_TO to {0}", job.getResponseQueueName());
                    exch.getIn().setHeader("rabbitmq.ROUTING_KEY", job.getJobDefinition().getQueuename());
                    exch.getIn().setHeader("rabbitmq.REPLY_TO", job.getResponseQueueName());
                })
                .split(body(), AggregationStrategies.useLatest()).streaming()
                //.log("Submitting ${body}")
                .to(ExchangePattern.InOnly, mqueueUrl + "&autoDelete=false")
                .end()
                .setBody(header("CamelSplitSize"))
                .process((Exchange exch) -> {
                    int count = exch.getIn().getBody(Integer.class);
                    AbstractDatasetJob job = JobHandler.getJob(exch, AbstractDatasetJob.class);
                    job.status = JobStatus.Status.RUNNING;
                    job.totalCount = count;
                    exch.getIn().setBody(count);
                })
                .log("Split and sent ${body} items");

        // a dummy queue for testing
        from(mqueueUrl + "&autoDelete=false&routingKey=queue1")
                .log("queue1 received ${body}, sending to " + mqueueUrl + "&autoDelete=false")
                .delay(1000)
                .setHeader("rabbitmq.ROUTING_KEY", header("rabbitmq.REPLY_TO"))
                .removeHeader("rabbitmq.REPLY_TO")
                .to(mqueueUrl + "&autoDelete=false")
                .log("Message ${body} processed and sent to ${headers[rabbitmq.ROUTING_KEY]}");

    }

    String getRabbitMQUrl(MessageQueueCredentials rabbitmqCredentials) {
        return "rabbitmq://" + rabbitmqCredentials.getHostname()
                + "/" + rabbitmqCredentials.getExchange()
                + "?vhost=" + rabbitmqCredentials.getVirtualHost()
                + "&username=" + rabbitmqCredentials.getUsername()
                + "&password=" + rabbitmqCredentials.getPassword();
    }
}
