package com.im.lac.demo.routes;

import org.apache.camel.builder.RouteBuilder;

/**
 * These are routes that provide examples of services. They are supposed to
 * illustrate what real-world services would need to do
 *
 * @author timbo
 */
public class RDKitRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:rdkit/predict")
                .log("rdkit/predict starting")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .setHeader("FUNCTION", constant("num_hba"))
                .to("language:python:classpath:calc_props_thread.py?transform=false")
                .log("rdkit/predict finished");

        from("direct:rdkit/filter")
                .log("rdkit/filter finished")
                .to("language:python:classpath:molecule_objects.py?transform=false")
                .setHeader("FUNCTION", constant("-1<num_hbd<6"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .setHeader("FUNCTION", constant("-1<num_hba<11"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .setHeader("FUNCTION", constant("-500<mol_logp<5"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .setHeader("FUNCTION", constant("0<mol_mr<500"))
                .to("language:python:classpath:filter_props_thread.py?transform=false")
                .log("rdkit/filter finished");
    }
}
