package com.im.lac.camel.chemaxon.routes;

import dataFormat.MoleculeIteratorDataFormat;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.util.URISupport;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Camel REST web service
 *
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder {

    public static final Integer DEFAULT_PORT = 8080;
    public final String host;
    public final Integer port;

    public RestRouteBuilder(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public RestRouteBuilder(Integer port) {
        this("localhost", DEFAULT_PORT);
    }

    public RestRouteBuilder() {
        this("localhost", DEFAULT_PORT);
    }

    @Override
    public void configure() throws Exception {

        MoleculeIteratorDataFormat molDataFormat = new MoleculeIteratorDataFormat();

        restConfiguration().component("restlet").host(host).port(port).bindingMode(RestBindingMode.off);

        // simples example
        rest("/ping").get()
                .route().transform().constant("Service Running");

        // This receives a POST request, processes it and returns the result
        rest("/atomCount").post()
                .to("direct:handleAtomCount");

        // This receives a POST request, processes it and returns the result
        rest("/dump").get()
                .to("direct:dump");

        from("direct:handleAtomCount")
                .to("direct:atomcount")
                .log("Calculations complete")
                .marshal(molDataFormat)
                .log("Marshalling complete");

        from("direct:dump")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        StringBuilder buf = new StringBuilder();
                        Map<String, Object> headers = exchange.getIn().getHeaders();
                        buf.append("------ Headers ------\n");
                        for (Map.Entry<String, Object> e : headers.entrySet()) {
                            buf.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
                        }

                        String uri = headers.get("CamelHttpUri").toString();
                        String query = headers.get("CamelHttpQuery").toString();
                        Map<String, Object> params = URISupport.parseQuery(query);
                        buf.append("------ Query params Camel ------\n");
                        for (Map.Entry<String, Object> e : params.entrySet()) {
                            buf.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
                        }
                        
                        List<NameValuePair> params2 = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
                        buf.append("------ Query params Apache HTTP ------\n");
                        for (NameValuePair nvp : params2) {
                            buf.append(nvp.getName()).append(" -> ").append(nvp.getValue()).append("\n");
                        }

                        System.out.println(buf.toString());
                        exchange.getIn().setBody(buf.toString());
                    }

                });

    }
}
