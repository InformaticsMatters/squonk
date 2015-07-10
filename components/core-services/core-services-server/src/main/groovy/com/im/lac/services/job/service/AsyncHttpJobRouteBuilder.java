package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.services.ServerConstants;
import static com.im.lac.services.job.service.AbstractAsyncJobRouteBuilder.ROUTE_ASYNC_SUBMIT;
import static com.im.lac.services.job.service.JobServiceRouteBuilder.ROUTE_SUBMIT_PREFIX;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class AsyncHttpJobRouteBuilder extends AbstractAsyncJobRouteBuilder {

    static final String ROUTE_DISPATCH_HTTP = "direct:dispatchHttp";

    protected static final String ROUTE_ASYNC_HTTP_SUBMIT = ROUTE_SUBMIT_PREFIX + AsyncHttpProcessDatasetJobDefinition.class.getName();

    @Override
    public void configure() throws Exception {

        super.configure();

        from(ROUTE_ASYNC_HTTP_SUBMIT)
                .setHeader(ServerConstants.HEADER_DESTINATION, simple("${body.destination}"))
                .to(ROUTE_ASYNC_SUBMIT);

        from(ROUTE_DISPATCH_HTTP)
                .setHeader(Exchange.HTTP_URI, constant(ServerConstants.HEADER_DESTINATION))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .to("http4:dummy")
                .log("HTTP response received");

    }

    @Override
    protected String getDispatchRoute() {
        return ROUTE_DISPATCH_HTTP;
    }
}
