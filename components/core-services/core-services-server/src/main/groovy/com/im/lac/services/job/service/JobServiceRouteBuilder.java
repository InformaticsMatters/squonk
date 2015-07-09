package com.im.lac.services.job.service;

import com.im.lac.services.camel.CamelLifeCycle;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author timbo
 */
public class JobServiceRouteBuilder extends RouteBuilder {

    protected static final String ROUTE_ROUTING_SLIP_HEADER = "JobSubmitRoutingSlip";
    public static final String ROUTE_SUBMIT_JOB = "seda:submitJob";
    protected static final String ROUTE_SUBMIT_PREFIX = "seda:job_submit_";

    @Override
    public void configure() throws Exception {

        // This is the entrypoint. Send your JobDefintion here to be executed.
        // Sends the job for execution and returns immediately with the appropriate JobStatus
        from(ROUTE_SUBMIT_JOB)
                .threads().executorServiceRef(CamelLifeCycle.CUSTOM_THREAD_POOL_NAME)
                .setExchangePattern(ExchangePattern.InOut)
                // body is the JobDefintion
                .log("Job defintion ${body} received")
                .setHeader(ROUTE_ROUTING_SLIP_HEADER, simple(ROUTE_SUBMIT_PREFIX + "${body.class.name}"))
                .log("Routing to ${header." + ROUTE_ROUTING_SLIP_HEADER + "}")
                .routingSlip(header(ROUTE_ROUTING_SLIP_HEADER))
                // body is now JobStatus
                .log("Job submitted. Current status is ${body}");

    }
}
