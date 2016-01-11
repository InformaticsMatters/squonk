package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.SplitAndQueueProcessDatasetJobDefinition;
import com.im.lac.services.ServerConstants;
import org.apache.camel.builder.RouteBuilder;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.types.io.JsonHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.im.lac.services.job.service.JobServiceRouteBuilder.ROUTE_SUBMIT_PREFIX;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_STEPS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_STEPS_EXCHANGE_PARAMS;

public class SplitAndQueueJobRouteBuilder extends RouteBuilder implements ServerConstants {

    private final MessageQueueCredentials rabbitmqCredentials;

    private static final Logger LOG = Logger.getLogger(SplitAndQueueJobRouteBuilder.class.getName());

    public static final String ROUTE_SPLIT_AND_QUEUE_SUBMIT = ROUTE_SUBMIT_PREFIX + SplitAndQueueProcessDatasetJobDefinition.class.getName();
    public static final String ROUTE_FETCH_AND_DISPATCH = "seda:queueProcessDatasetSubmit";
    public static final String ENDPOINT_SPLIT_AND_SUBMIT = "seda:splitAndSubmit";
    public static final String DUMMY_MESSAGE_QUEUE = "rabbitmq";
    public static final String ROUTE_STEPS_JOB_SUBMIT = "direct:stepsJobSubmit";

    JsonHandler jsonHandler = new JsonHandler();
    //MoleculeObjectJsonDataFormat moDataFormat = new MoleculeObjectJsonDataFormat();

    public SplitAndQueueJobRouteBuilder() {
        this.rabbitmqCredentials = new MessageQueueCredentials();
    }

    public SplitAndQueueJobRouteBuilder(MessageQueueCredentials rabbitmqCredentials) {
        this.rabbitmqCredentials = rabbitmqCredentials;
    }

    @Override
    public void configure() throws Exception {

        String mqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_JOB_STEPS_EXCHANGE_NAME, MQUEUE_JOB_STEPS_EXCHANGE_PARAMS);
        LOG.log(Level.INFO, "Using RabbitMQ URL of {0}", mqueueUrl);

        // This is the entrypoint. Send your AbstractDatasetJob here.
        // Sends the job for execution and returns immediately with the Job status updated accordingly
//        from(ROUTE_SPLIT_AND_QUEUE_SUBMIT)
//                // body is the jobdef
//                .log("ROUTE_SPLIT_AND_QUEUE_SUBMIT Processing dataset using queue submit ... ${body}")
//                .setHeader(HEADER_DATASET_ID, simple("${body.datasetId}"))
//                // create the job
//                .process((Exchange exch) -> {
//                    SplitAndQueueProcessDatasetJobDefinition jobdef = exch.getIn().getBody(SplitAndQueueProcessDatasetJobDefinition.class);
//                    SplitAndQueueJob job = new SplitAndQueueJob(jobdef);
//                    job.status = JobStatus.Status.PENDING;
//                    JobHandler.getJobStore(exch).putJob(job);
//                    exch.getIn().setBody(job);
//                    exch.getIn().setHeader(REST_JOB_ID, job.getJobId());
//                })
//                // send for execution
//                .to(ExchangePattern.InOnly, ROUTE_FETCH_AND_DISPATCH)
//                // job is now running
//                .process((Exchange exch) -> {
//                    // set the updated status as the body
//                    AbstractDatasetJob job = exch.getIn().getBody(AbstractDatasetJob.class);
//                    job.status = JobStatus.Status.PENDING;
//                    JobHandler.getJobStore(exch).putJob(job);
//                    exch.getIn().setBody(job.buildStatus());
//                    exch.getIn().setHeader(REST_JOB_ID, job.getJobId());
//                })
//                // body is now JobStatus
//                .log("Queue submit complete")
        ;

        // This is where most of the work is done. The dataset is retreived and then forwarded to the
        // specified endpoint. the endpoint returns the results and these are then forwarded on to be saved. 
//        from(ROUTE_FETCH_AND_DISPATCH)
//                .log("ROUTE_FETCH_AND_DISPATCH")
//                // body is the job
//                .setExchangePattern(ExchangePattern.InOut)
//                // fetch the dataset to process
//                .process((Exchange exch) -> JobHandler.setBodyAsObjectsForDataset(exch))
//                // body is now the dataset
//                // send be split and submitted
//                .to(ENDPOINT_SPLIT_AND_SUBMIT)
//                .process((Exchange exch) -> JobHandler.putCurrentJobStatus(exch))
//                .log("Job is being processed")
        ;

        // splits the body and sends each item to the JMS queue specified by the CamelJmsDestinationName header
        // returns the number of items split/posted as the body
//        from(ENDPOINT_SPLIT_AND_SUBMIT)
//                .log("ENDPOINT_SPLIT_AND_SUBMIT Splitting and submitting ${body}")
//                .process((Exchange exch) -> {
//                    SplitAndQueueJob job = JobHandler.getJobFromHeader(exch, SplitAndQueueJob.class);
//                    job.status = JobStatus.Status.SUBMITTING;
//                    LOG.log(Level.INFO, "Setting ROUTING_KEY to {0}", job.getJobDefinition().getQueuename());
//                    LOG.log(Level.INFO, "Setting REPLY_TO to {0}", job.getResponseQueueName());
//                    exch.getIn().setHeader("rabbitmq.ROUTING_KEY", job.getJobDefinition().getQueuename());
//                    exch.getIn().setHeader("rabbitmq.REPLY_TO", job.getResponseQueueName());
//                })
//                .log("About to split ${body.class.name}")
//                .convertBodyTo(Iterator.class)
//                .split(body(), AggregationStrategies.useLatest()).streaming()
//                .process((Exchange exch) -> {
//                    Object body = exch.getIn().getBody();
//                    Metadata meta = new Metadata();
//                    String json = jsonHandler.marshalItemAsString(body, meta);
//                    exch.getIn().setBody(json);
//                    String metaJson = jsonHandler.objectToJson(meta);
//                    exch.getIn().setHeader(CamelCommonConstants.HEADER_METADATA, metaJson);
//                })
//                .log("Submitting ${body}")
//                .to(ExchangePattern.InOnly, mqueueUrl + "&autoDelete=false&durable=true")
//                .end()
//                .setBody(header("CamelSplitSize"))
//                .process((Exchange exch) -> {
//                    int count = exch.getIn().getBody(Integer.class);
//                    AbstractDatasetJob job = JobHandler.getJobFromHeader(exch, AbstractDatasetJob.class);
//                    job.status = JobStatus.Status.RUNNING;
//                    job.totalCount = count;
//                    exch.getIn().setBody(count);
//                })
//                .log("Split and sent ${body} items")
        ;

        // a dummy queue for testing
//        from(mqueueUrl + "&autoDelete=false&queue=devnull")
//                .log("devnull received ${body}, with rabbitmq.REPLY_TO of ${header[rabbitmq.REPLY_TO]}");

//        from(ROUTE_STEPS_JOB_SUBMIT)
//                .log("Submitting stepped job to queue " + mqueueUrl + ": ${body}")
//                .to(ExchangePattern.InOnly, mqueueUrl)
//                .log("Job submitted");

    }
}
