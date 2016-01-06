package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.*;
import com.im.lac.services.job.Job;
import com.im.lac.services.job.dao.MemoryJobStatusClient;
import com.im.lac.services.util.Utils;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.squonk.client.JobStatusClient;

/**
 *
 * @author timbo
 */
public class JobServiceRouteBuilder extends RouteBuilder {

    protected static final String ROUTE_ROUTING_SLIP_HEADER = "JobSubmitRoutingSlip";
    public static final String ROUTE_SUBMIT_JOB = "seda:submitJob";
    protected static final String ROUTE_SUBMIT_PREFIX = "seda:job_submit_";
    public static final String ROUTE_DO_NOTHING = ROUTE_SUBMIT_PREFIX + DoNothingJobDefinition.class.getName();

    private final JobStatusClient jobStatusClient = new MemoryJobStatusClient();

    @Override
    public void configure() throws Exception {

        // This is the entrypoint. Send your JobDefintion here to be executed.
        // Sends the job for execution and returns immediately with the appropriate JobStatus
//        from(ROUTE_SUBMIT_JOB)
//                .log("ROUTE_SUBMIT_JOB")
//                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
//                .setExchangePattern(ExchangePattern.InOut)
//                // body is the JobDefintion
//                .log("Job defintion ${body} received")
//                .log("Routing to " + ROUTE_SUBMIT_PREFIX + "${body.class.name}")
//                .setHeader(ROUTE_ROUTING_SLIP_HEADER, simple(ROUTE_SUBMIT_PREFIX + "${body.class.name}"))
//                .log("Routing to ${header." + ROUTE_ROUTING_SLIP_HEADER + "}")
//                .routingSlip(header(ROUTE_ROUTING_SLIP_HEADER))
//                // body is now JobStatus
//                .log("Job submitted. Current status is ${body}");

        from(ROUTE_SUBMIT_JOB)
                .log("ROUTE_SUBMIT_JOB")
                .process((Exchange exch) -> {
                    JobDefinition jobdef = exch.getIn().getBody(JobDefinition.class);
                    Job job = null;
                    if (jobdef instanceof AsyncHttpProcessDatasetJobDefinition) {
                        job = new AsyncHttpJob((AsyncHttpProcessDatasetJobDefinition)jobdef);
                    } else if (jobdef instanceof AsyncLocalProcessDatasetJobDefinition) {
                        job = new AsyncLocalJob((AsyncLocalProcessDatasetJobDefinition)jobdef);
                    } else if (jobdef instanceof ExecuteCellUsingStepsJobDefinition) {
                        job = new StepsCellJob((StepsCellExecutorJobDefinition)jobdef, jobStatusClient);
                    } else if (jobdef instanceof DoNothingJobDefinition) {
                        job = new DoNothingJob((DoNothingJobDefinition)jobdef);
                    } else {
                        throw new IllegalStateException("Job definition type " + jobdef.getClass().getName() + " not currently supported");
                    }
                    JobStatus status = job.start(exch.getContext(), Utils.fetchUsername(exch));
                    exch.getIn().setBody(status);
                });

        from(ROUTE_DO_NOTHING)
                .log("ROUTE_DO_NOTHING")
                .process((Exchange exch) -> {
                    DoNothingJobDefinition jobdef = exch.getIn().getBody(DoNothingJobDefinition.class);
                    Job job = new DoNothingJob(jobdef);
                    JobHandler.getJobStore(exch).putJob(job);
                    JobStatus status = job.getCurrentJobStatus();
                    exch.getIn().setBody(status);
                });

    }
}
