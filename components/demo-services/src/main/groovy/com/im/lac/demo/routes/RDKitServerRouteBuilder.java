package com.im.lac.demo.routes;

import com.im.lac.camel.dataformat.MoleculeObjectJsonConverter;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * These are routes that provide examples of services. They are supposed to
 * illustrate what real-world services would need to do
 *
 * @author timbo
 */
public class RDKitServerRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:rdkitserver/cluster")
                .log("rdkitserver/cluster starting")
                .marshal(new MoleculeObjectJsonConverter())
                .convertBodyTo(String.class)
                .log("Clustering ${body}")
                //.setHeader(Exchange.HTTP_URI, constant("http://localhost:8000/rdkit_cluster/cluster_simple/?fp_method=morgan&sim_method=tanimoto&threshold=0.5"))
                .setHeader(Exchange.HTTP_QUERY, constant("fp_method=morgan&sim_method=tanimoto&threshold=0.5"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                //.inOut("http4://localhost:8000/rdkit_cluster/cluster_simple/")
                .inOut("http4://localhost:8080/rest/test")
                .unmarshal(new MoleculeObjectJsonConverter())
                .log("rdkitserver/screen finished");
        
//        from("direct:rdkitserver/cluster")
//                .log("rdkitserver/cluster starting")
//                .marshal(new MoleculeObjectJsonConverter())
//                .convertBodyTo(String.class)
//                .log("Clustering ${body}")
//                .setHeader(Exchange.HTTP_URI, constant("http://localhost:8000/rdkit_cluster/cluster_simple/"))
//                //.setHeader(Exchange.HTTP_QUERY, constant("fp_method=morgan&sim_method=tanimoto&threshold=0.5"))
//                .removeHeader(Exchange.HTTP_QUERY)
//                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
//                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
//                .to("jetty://http://localhost:8000/rdkit_cluster/cluster_simple/")
//                .unmarshal(new MoleculeObjectJsonConverter())
//                .log("rdkitserver/screen finished");

        rest("/rest/test")
                .post()
                .route()
                .to("direct:/dump/exchange")
                .log("query: ${header.CamelHttpQuery}")
                .log("body: ${body}")
                .setBody(constant("Goodbye World!"));

        from("timer://foo?fixedRate=true&period=60000")
                .setBody(simple("Hello World! ${date:now:h:mm:ss a}"))
                .setHeader(Exchange.HTTP_QUERY, constant("foo=bar"))
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .inOut("http4://localhost:8080/rest/test")
                .log("Result: ${body}");

    }
}
