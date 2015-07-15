package com.im.lac.services.job.service;

import com.im.lac.services.ServerConstants;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.camel.CamelCommonConstants;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;

public abstract class AbstractAsyncJobRouteBuilder extends RouteBuilder implements ServerConstants {

    protected static final String ROUTE_ASYNC_SUBMIT = "direct:asyncSubmit";
    public static final String ROUTE_FETCH_AND_DISPATCH = "direct:asyncProcessDatasetSubmit";
    public static final String ROUTE_HANDLE_RESULTS = "direct:asyncProcessDatasetHandleResults";
    public static final String ROUTE_DUMMY = "direct:simpleroute";

    protected abstract String getDispatchRoute();

    @Override
    public void configure() throws Exception {

        // For ProcessDatasetJobDefinition
        from(ROUTE_ASYNC_SUBMIT)
                .log("ROUTE_ASYNC_SUBMIT")
                // body is job
                .log("Async submit. Body: ${body}")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .setExchangePattern(ExchangePattern.InOnly)
                .process((Exchange exch) -> {
                    AbstractDatasetJob job = exch.getIn().getBody(AbstractDatasetJob.class);
                    job.status = JobStatus.Status.PENDING;
                    JobHandler.getJobStore(exch).putJob(job);
                    exch.getIn().setHeader(HEADER_DATASET_ID, job.getJobDefinition().getDatasetId());
                    exch.getIn().setHeader(REST_JOB_ID, job.getJobId());
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
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                // body is the job
                .setExchangePattern(ExchangePattern.InOut)
                .process((Exchange exch) -> JobHandler.setJobStatus(exch, JobStatus.Status.RUNNING))
                .log("submit to endpoint ${header." + HEADER_DESTINATION + "}")
                // fetch the dataset to process
                .process((Exchange exch) -> JobHandler.setBodyAsObjectsForDataset(exch))
                // send  to the desired endpoint async
                .to(getDispatchRoute())
                // now handle the results
                .to(ExchangePattern.InOnly, ROUTE_HANDLE_RESULTS)
                .log("asyncProcessDatasetSubmit complete");

        // Save the results to the database
        from(ROUTE_HANDLE_RESULTS)
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                // body is the result of the execution - a stream - do not log it or it will be consumed
                .log("Handling results for job ${header." + REST_JOB_ID + "}")
                .process((Exchange exch) -> JobHandler.saveDatasetForJob(exch))
                .log("Results handled");
        // body is now the job status

        from(ROUTE_DUMMY).
                log("Dummy received ${body}")
                .delay(500);

    }
}
