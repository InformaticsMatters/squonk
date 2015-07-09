package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.AsyncProcessDatasetJobDefinition;
import com.im.lac.services.ServerConstants;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.services.CommonConstants;
import com.im.lac.services.camel.CamelLifeCycle;
import static com.im.lac.services.job.service.JobServiceRouteBuilder.ROUTE_SUBMIT_PREFIX;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;

public class AsyncLocalJobRouteBuilder extends RouteBuilder {

    protected static final String ROUTE_ASYNC_SUBMIT = ROUTE_SUBMIT_PREFIX + AsyncProcessDatasetJobDefinition.class.getName();
    public static final String ROUTE_FETCH_AND_DISPATCH = "direct:asyncProcessDatasetSubmit";
    public static final String ROUTE_HANDLE_RESULTS = "direct:asyncProcessDatasetHandleResults";
    public static final String ROUTE_DUMMY = "direct:simpleroute";

    @Override
    public void configure() throws Exception {

        // For ProcessDatasetJobDefinition
        from(ROUTE_ASYNC_SUBMIT)
                .threads().executorServiceRef(CamelLifeCycle.CUSTOM_THREAD_POOL_NAME)
                .setExchangePattern(ExchangePattern.InOnly)
                // body is jobdef
                .log("Async submit. Body: ${body}")
                .setHeader(ServerConstants.HEADER_DESTINATION, simple("${body.destination}"))
                .setHeader(ServerConstants.HEADER_DATASET_ID, simple("${body.datasetId}"))
                // create the job
                .process((Exchange exch) -> {
                    AsyncProcessDatasetJobDefinition jobdef = exch.getIn().getBody(AsyncProcessDatasetJobDefinition.class);
                    AsyncJob job = new AsyncJob(jobdef);
                    job.status = JobStatus.Status.PENDING;
                    JobHandler.getJobStore(exch).putJob(job);
                    exch.getIn().setBody(job);
                    exch.getIn().setHeader(CommonConstants.HEADER_JOB_ID, job.getJobId());
                })
                // body is now the job
                // send for async execution
                .to(ExchangePattern.InOnly, ROUTE_FETCH_AND_DISPATCH)
                // job is now running
                .process((Exchange exch) -> JobHandler.putCurrentJobStatus(exch))
                // body is now JobStatus
                .log("Job is executing");

        // This is where most of the work is done. The dataset is retreived and then forwarded to the
        // specified endpoint. the endpoint returns the results and these are then forwarded on to be saved. 
        from(ROUTE_FETCH_AND_DISPATCH)
                .threads().executorServiceRef(CamelLifeCycle.CUSTOM_THREAD_POOL_NAME)
                // body is the job
                .setExchangePattern(ExchangePattern.InOut)
                .process((Exchange exch) -> JobHandler.setJobStatus(exch, JobStatus.Status.RUNNING))
                .log("submit to endpoint ${header." + ServerConstants.HEADER_DESTINATION + "}")
                // fetch the dataset to process
                .process((Exchange exch) -> JobHandler.setBodyAsObjectsForDataset(exch))
                // send  to the desired endpoint async
                .routingSlip(header(ServerConstants.HEADER_DESTINATION))
                .log("Routing slip completed")
                // now handle the results
                .to(ExchangePattern.InOnly, ROUTE_HANDLE_RESULTS)
                .log("asyncProcessDatasetSubmit complete");

        // Save the results to the database
        from(ROUTE_HANDLE_RESULTS)
                .threads().executorServiceRef(CamelLifeCycle.CUSTOM_THREAD_POOL_NAME)
                // body is the result of the execution - a stream - do not log it or it will be consumed
                .log("Handling results for job ${header." + ServerConstants.HEADER_JOB_ID + "}")
                .process((Exchange exch) -> JobHandler.saveDatasetForJob(exch))
                .log("Results handled");
                // body is now the job status

        // A mock route that does nothing except for adding a delay for testing purposes
        from(ROUTE_DUMMY)
                .threads().executorServiceRef(CamelLifeCycle.CUSTOM_THREAD_POOL_NAME)
                .log("executing simpleroute")
                .delay(500)
                .log("simpleroute complete");
    }
}
