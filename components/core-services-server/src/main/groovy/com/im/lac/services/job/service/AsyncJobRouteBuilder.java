package com.im.lac.services.job.service;

import com.im.lac.services.ServerConstants;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.camel.CamelCommonConstants;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.job.jobdef.AsyncLocalProcessDatasetJobDefinition;
import static com.im.lac.services.job.service.JobServiceRouteBuilder.ROUTE_SUBMIT_PREFIX;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;

public class AsyncJobRouteBuilder extends RouteBuilder implements ServerConstants {

    public static final Logger LOG = Logger.getLogger(AsyncJobRouteBuilder.class.getName());

    protected static final String ROUTE_ASYNC_SUBMIT = "direct:asyncSubmit";
    public static final String ROUTE_FETCH_AND_DISPATCH = "direct:asyncProcessDatasetSubmit";
    public static final String ROUTE_HANDLE_RESULTS = "direct:asyncProcessDatasetHandleResults";
    public static final String ROUTE_DUMMY = "direct:simpleroute";
    private static final String HEADER_DISPATCH_MODE = "DispatchMode";

    // Local specific
    static final String ROUTE_DISPATCH_LOCAL = "direct:dispatchLocal";
    static final String ROUTE_ASYNC_LOCAL_SUBMIT = ROUTE_SUBMIT_PREFIX + AsyncLocalProcessDatasetJobDefinition.class.getName();

    // Http specific
    static final String ROUTE_DISPATCH_HTTP = "direct:dispatchHttp";
    static final String ROUTE_ASYNC_HTTP_SUBMIT = ROUTE_SUBMIT_PREFIX + AsyncHttpProcessDatasetJobDefinition.class.getName();

    public static final String ROUTE_HTTP_SUBMIT = "direct:httpSubmit";

    @Override
    public void configure() throws Exception {

        // common stuff
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
                .routingSlip(header(HEADER_DISPATCH_MODE))
                // now handle the results
                .to(ExchangePattern.InOnly, ROUTE_HANDLE_RESULTS)
                .log("asyncProcessDatasetSubmit complete");

        // Save the results to the database
        from(ROUTE_HANDLE_RESULTS)
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                // body is the result of the execution - a stream - do not log it or it will be consumed
                .log("Handling results for job ${header." + REST_JOB_ID + "}")
                .process((Exchange exch) -> JobHandler.saveDatasetForJob(exch))
                // body is now the job status
                .log("Results handled");

        from(ROUTE_DUMMY)
                .log("Dummy received ${body.class.name}")
                .delay(500);

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Local routes
        //////////////////////////////////////////////////////////////////////////////////////////////
        from(ROUTE_ASYNC_LOCAL_SUBMIT)
                .log("ROUTE_ASYNC_LOCAL_SUBMIT")
                .setHeader(ServerConstants.HEADER_DESTINATION, simple("${body.endpoint}"))
                .setHeader(HEADER_DISPATCH_MODE, constant(ROUTE_DISPATCH_LOCAL))
                .process((Exchange exch) -> {
                    AsyncLocalProcessDatasetJobDefinition jobdef = exch.getIn().getBody(AsyncLocalProcessDatasetJobDefinition.class);
                    AsyncLocalJob job = new AsyncLocalJob(jobdef);
                    exch.getIn().setBody(job);
                })
                .to(ROUTE_ASYNC_SUBMIT);

        from(ROUTE_DISPATCH_LOCAL)
                .log("ROUTE_DISPATCH_LOCAL")
                .routingSlip(header(ServerConstants.HEADER_DESTINATION))
                .log("Routing slip completed");

        //////////////////////////////////////////////////////////////////////////////////////////////
        // HTTP specific routes
        //////////////////////////////////////////////////////////////////////////////////////////////
        from(ROUTE_ASYNC_HTTP_SUBMIT)
                .log("ROUTE_ASYNC_HTTP_SUBMIT received for ${body.destination}")
                .setHeader(ServerConstants.HEADER_DESTINATION, simple("${body.destination}"))
                .setHeader(HEADER_DISPATCH_MODE, constant(ROUTE_DISPATCH_HTTP))
                .process((Exchange exch) -> {
                    AsyncHttpProcessDatasetJobDefinition jobdef = exch.getIn().getBody(AsyncHttpProcessDatasetJobDefinition.class);
                    AsyncHttpJob job = new AsyncHttpJob(jobdef);
                    exch.getIn().setBody(job);
                })
                .to(ROUTE_ASYNC_SUBMIT);

        from(ROUTE_DISPATCH_HTTP)
                .log("ROUTE_DISPATCH_HTTP received")
                .setHeader(Exchange.HTTP_URI, header(ServerConstants.HEADER_DESTINATION))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .log("Routing to ${header[" + ServerConstants.HEADER_DESTINATION + "]}")
                .to("http4:dummy")
                .log("HTTP response received");

        from(ROUTE_HTTP_SUBMIT)
                .log("ROUTE_HTTP_SUBMIT received")
                .log("Routing to ${header[" + Exchange.HTTP_URI + "]}")
                .to("http4:dummy")
                .log("HTTP response received");

    }

}
