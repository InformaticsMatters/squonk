package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.AsyncLocalProcessDatasetJobDefinition;
import static com.im.lac.services.job.service.JobServiceRouteBuilder.ROUTE_SUBMIT_PREFIX;
import com.im.lac.services.ServerConstants;

public class AsyncLocalJobRouteBuilder extends AbstractAsyncJobRouteBuilder {

    static final String ROUTE_DISPATCH_LOCAL = "direct:dispatchLocal";
    protected static final String ROUTE_ASYNC_LOCAL_SUBMIT = ROUTE_SUBMIT_PREFIX + AsyncLocalProcessDatasetJobDefinition.class.getName();

    @Override
    public void configure() throws Exception {

        super.configure();

        from(ROUTE_ASYNC_LOCAL_SUBMIT)
                .setHeader(ServerConstants.HEADER_DESTINATION, simple("${body.endpoint}"))
                .to(ROUTE_ASYNC_SUBMIT);

        from(ROUTE_DISPATCH_LOCAL)
                .routingSlip(header(ServerConstants.HEADER_DESTINATION))
                .log("Routing slip completed");

    }

    @Override
    protected String getDispatchRoute() {
        return ROUTE_DISPATCH_LOCAL;
    }
}
