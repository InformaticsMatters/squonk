package com.im.lac.demo.routes;

import com.im.lac.camel.chemaxon.processor.enumeration.ReactorProcessor;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author timbo
 */
public class ReactorRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:reactor")
                .log("reactor starting")
                .process(new ReactorProcessor())
                .log("reactor finished");
    }

}
