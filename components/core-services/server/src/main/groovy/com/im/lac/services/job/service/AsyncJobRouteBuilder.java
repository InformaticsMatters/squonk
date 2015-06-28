package com.im.lac.services.job.service;

import com.im.lac.services.camel.Constants;
import com.im.lac.services.job.JobStatus;
import com.im.lac.services.job.AbstractDatasetJob;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;

public class AsyncJobRouteBuilder extends RouteBuilder {

    public static final String ROUTE_SUBMIT = "seda:asyncProcessDatasetStart";
    public static final String ROUTE_FETCH_AND_DISPATCH = "seda:asyncProcessDatasetSubmit?concurrentConsumers=5";
    public static final String ROUTE_HANDLE_RESULTS = "seda:asyncProcessDatasetHandleResults?concurrentConsumers=5";
    public static final String ROUTE_DUMMY = "seda:simpleroute?concurrentConsumers=5";

    @Override
    public void configure() throws Exception {

        // This is the entrypoint. Send your AbstractDatasetJob here.
        // Sends the job for execution and returns immediately with the Job status updated accordingly
        from(ROUTE_SUBMIT)
                // body is the job
                .log("processDataset ... ${body}")
                .setHeader(Constants.HEADER_DESTINATION, simple("${body.jobDefinition.destination}"))
                .setHeader(Constants.HEADER_DATASET_ID, simple("${body.jobDefinition.datasetId}"))
                .log("Endpoint set to ${header.endpoint}")
                .setHeader("JobId", simple("${body.jobId}"))
                .to(ExchangePattern.InOnly, ROUTE_FETCH_AND_DISPATCH) // send for async execution
                // job is now running
                .process((Exchange exchange) -> {
                    // set the updated status as the body
                    AbstractDatasetJob job = (AbstractDatasetJob) exchange.getIn().getBody(AbstractDatasetJob.class);
                    job.setStatus(JobStatus.Status.RUNNING);
                    exchange.getIn().setBody(job.buildStatus());
                })
                .log("asyncProcessDatasetStart complete");

        // This is where most of the work is done. The dataset is retreived and then forwarded to the
        // specified endpoint. the endpoint returns the results and these are then forwarded on to be saved. 
        from(ROUTE_FETCH_AND_DISPATCH)
                // body is the job
                .setExchangePattern(ExchangePattern.InOut)
                .log("submit to endpoint ${header." + Constants.HEADER_DESTINATION + "}")
                // fetch the dataset to process
                .process((Exchange exch) -> JobHandler.setBodyAsObjectsForDataset(exch))
                // send  to the desired endpoint async
                .routingSlip(header(Constants.HEADER_DESTINATION))
                // now handle the results
                .to(ROUTE_HANDLE_RESULTS)
                .log("asyncProcessDatasetSubmit complete");

        // Save the results to the database
        from(ROUTE_HANDLE_RESULTS)
                // body is the result of the execution
                .log("Handling results for job ${header." + Constants.HEADER_JOB_ID + "}: ${body}")
                .process((Exchange exch) -> JobHandler.saveDatasetForJob(exch));
                // body is now the job status

        // A mock route that does nothing except for adding a delay for testing purposes
        from(ROUTE_DUMMY)
                .log("executing simpleroute")
                .delay(1000)
                .transform(constant("results goes here"))
                .log("simpleroute complete");
    }
}
