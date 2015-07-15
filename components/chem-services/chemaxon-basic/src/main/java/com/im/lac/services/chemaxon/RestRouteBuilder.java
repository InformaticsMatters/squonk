package com.im.lac.services.chemaxon;

import com.im.lac.camel.util.CamelUtils;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;


/**
 *
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RestRouteBuilder.class.getName());


    @Override
    public void configure() throws Exception {

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("/rest/ping")
                .get().description("Simple ping service to check things are running")
                .produces("text/plain")
                .route()
                .transform(constant("Ping\n")).endRest();

        rest("/rest/v1").description("Job submission and management services")
                //
                // logP calculator
                .post("logp").description("Add the calculated logP to the supplied MoleculeObjects")
                .bindingMode(RestBindingMode.off)
                .consumes("application/json")
                .produces("application/json")
                .route()
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamInput(exch))
                .to(CalculatorsRouteBuilder.CHEMAXON_LOGP)
                .process((Exchange exch) -> CamelUtils.handleMoleculeObjectStreamOutput(exch))
                .endRest();

    }



}
