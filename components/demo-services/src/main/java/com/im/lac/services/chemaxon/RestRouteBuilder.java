package com.im.lac.services.chemaxon;

import chemaxon.jchem.db.cache.CacheManager;
import com.im.lac.services.processor.StaticContentProcessor;
import dataFormat.MoleculeIteratorDataFormat;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.util.URISupport;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Camel REST web service
 *
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder {

    public static final Integer DEFAULT_PORT = 8080;
    public final String host;
    public final Integer port;
    public final String docRoot;
    Map<String, Object> registry;

    public RestRouteBuilder(Map<String, Object> registry, String host, Integer port) {
        this.registry = registry;
        this.host = host;
        this.port = port;
        String root = System.getenv("HTML_DOC_ROOT");
        if (root == null) {
            root = "src/main/html/";
        }
        System.out.println("Doc Root = " + root);
        docRoot = root;
    }

    public RestRouteBuilder(Map<String, Object> registry, Integer port) {
        this(registry, "localhost", DEFAULT_PORT);
    }

    public RestRouteBuilder(Map<String, Object> registry) {
        this(registry, "localhost", DEFAULT_PORT);
    }

    @Override
    public void configure() throws Exception {

        MoleculeIteratorDataFormat molDataFormat = new MoleculeIteratorDataFormat();

        restConfiguration().component("jetty").host("0.0.0.0").port(port);
        
        from("jetty:http://0.0.0.0:8080/static?matchOnUriPrefix=true")
                .process(new StaticContentProcessor(docRoot));

        // simplest example
        rest("/rest/ping").get()
                .route().transform().constant("Service Running\n");

        // info on the JChem structure cache
        rest("/rest/cache").get()
                .route().process(new Processor() {

                    @Override
                    public void process(Exchange exchange) throws Exception {
                        StringBuilder b = new StringBuilder("Cache details:\n");
                        Hashtable<String,Long> tables = CacheManager.INSTANCE.getCachedTables();
                        for (Map.Entry<String,Long> e : tables.entrySet()) {
                            b.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
                        }
                        exchange.getIn().setBody(b.toString());
                    }

                });

        // This receives a POST request, processes it and returns the result
        rest("/rest/atomCount").post()
                .route()
                .to("direct:atomcount")
                .marshal(molDataFormat);

        rest("/rest/lipinski").post()
                .route()
                .to("direct:lipinski")
                .marshal(molDataFormat);

        rest("/rest/filter").post()
                .route()
                .to("direct:filter_example")
                .marshal(molDataFormat);

        rest("/rest/chemterms").post()
                .route()
                .to("direct:chemTerms")
                .marshal(molDataFormat);

        rest("/rest/chemsearch/emolecules_sc").post()
                .route()
                .to("direct:chemsearch/emolecules_sc")
                .marshal(molDataFormat);

        rest("/rest/dump").get()
                .to("direct:dump");

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
