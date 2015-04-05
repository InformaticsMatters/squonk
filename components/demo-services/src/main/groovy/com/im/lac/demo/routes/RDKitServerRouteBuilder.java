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
                .setHeader(Exchange.HTTP_QUERY, constant("fp_method=morgan&sim_method=tanimoto&threshold=0.5"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .inOut("http4://localhost:8000/rdkit_cluster/cluster_simple/")
                .unmarshal(new MoleculeObjectJsonConverter())
                .log("rdkitserver/screen finished");

    }
}
