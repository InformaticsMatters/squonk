package com.im.lac.demo.routes;

import com.im.lac.camel.dataformat.MoleculeObjectJsonConverter;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * Examples of calling RDKit REST services
 *
 * @author timbo
 */
public class RDKitServerRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:rdkitserver/cluster")
                .log("rdkitserver/cluster starting")
                .marshal(new MoleculeObjectJsonConverter())
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_QUERY, simple("fp_method=${header.fp_method}&sim_method=${header.sim_method}&threshold=${header.threshold}"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .inOut("http4://localhost:8000/rdkit_cluster/cluster_simple/")
                .unmarshal(new MoleculeObjectJsonConverter())
                .log("rdkitserver/screen finished");
        
        from("direct:rdkitserver/screen")
                .log("rdkitserver/screen starting")
                .marshal(new MoleculeObjectJsonConverter())
                .removeHeader(Exchange.HTTP_URI)
                .setHeader(Exchange.HTTP_QUERY, simple("fp_method=${header.fp_method}&sim_method=${header.sim_method}&threshold=${header.threshold}&smiles=${header.smiles}"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .inOut("http4://localhost:8000/rdkit_screen/screen_simple/")
                .unmarshal(new MoleculeObjectJsonConverter())
                .log("rdkitserver/screen finished");

    }
}
