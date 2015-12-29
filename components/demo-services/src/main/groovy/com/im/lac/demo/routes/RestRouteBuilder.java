package com.im.lac.demo.routes;

import chemaxon.jchem.db.cache.CacheManager;
import org.squonk.camel.chemaxon.dataformat.MoleculeObjectIteratorDataFormat;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.URISupport;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Camel REST web service
 *
 * @author timbo
 */
public class RestRouteBuilder extends RouteBuilder {

    public final String docRoot;

    public RestRouteBuilder() {
        String root = System.getenv("HTML_DOC_ROOT");
        if (root == null) {
            root = "src/main/html/";
        }
        System.out.println("Doc Root = " + root);
        docRoot = root;
    }

    @Override
    public void configure() throws Exception {

        MoleculeObjectIteratorDataFormat molDataFormat = new MoleculeObjectIteratorDataFormat();

        // simplest example
        rest("/rest/ping").get()
                .route()
                .log("pinged")
                .wireTap("direct:logger")
                .transform().constant("Service Running\n");

        // info on the JChem structure cache
        rest("/rest/cache").get()
                .route().process((Exchange exchange) -> {
                    StringBuilder b = new StringBuilder("Cache details:\n");
                    Map<String, Long> tables = CacheManager.INSTANCE.getCachedTables();
                    tables.entrySet().stream().forEach((e) -> {
                        b.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
                    });
                    exchange.getIn().setBody(b.toString());
                });

        // This receives a POST request, processes it and returns the result
        rest("/rest/atomCount").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:atomcount")
                .marshal(molDataFormat);

        rest("/rest/lipinski").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:lipinski")
                .marshal(molDataFormat);

        rest("/rest/filter").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:filter_example")
                .marshal(molDataFormat);

        rest("/rest/chemterms").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:chemTerms")
                .marshal(molDataFormat);

        rest("/rest/screening/ecfp").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:screening/ecfp")
                .marshal(molDataFormat);

        rest("/rest/screening/pharmacophore").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:screening/pharmacophore")
                .marshal(molDataFormat);

        rest("/rest/clustering/spherex/ecfp4").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:clustering/spherex/ecfp4")
                .marshal(molDataFormat);

        rest("/rest/chemsearch/emolecules_sc").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:chemsearch/emolecules_sc")
                .marshal(molDataFormat);

        rest("/rest/chemsearch/emolecules_bb").post()
                .route()
                .wireTap("direct:logger")
                .to("direct:chemsearch/emolecules_bb")
                .marshal(molDataFormat);

        rest("/rest/dump").get()
                .to("direct:dump");

        from("direct:dump")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        StringBuilder buf = new StringBuilder();

                        HttpServletRequest req = exchange.getIn().getHeader(Exchange.HTTP_SERVLET_REQUEST, HttpServletRequest.class);
                        if (req != null) {
                            buf.append("------ HttpServletRequest properties ------\n");

                            buf.append("Remote Address: ").append(req.getRemoteAddr()).append("\n");
                            buf.append("Remote Host: ").append(req.getRemoteHost()).append("\n");

                            buf.append("------ HttpServletRequest Headers ------\n");

                            Enumeration<String> headers = req.getHeaderNames();
                            while (headers.hasMoreElements()) {
                                String name = headers.nextElement();
                                Enumeration<String> values = req.getHeaders(name);
                                while (values.hasMoreElements()) {
                                    String value = values.nextElement();
                                    buf.append(name).append(" -> ").append(value).append("\n");
                                }
                            }
                        }

                        Map<String, Object> headers = exchange.getIn().getHeaders();
                        buf.append("------ Headers ------\n");
                        for (Map.Entry<String, Object> e : headers.entrySet()) {
                            buf.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
                        }

                        String uri = headers.get("CamelHttpUri").toString();
                        String query = (String) headers.get("CamelHttpQuery");
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

                }
                );

        from("direct:logger")
                .transform().simple("${date:now:yyyy.MM.dd}\t${date:now:HH:mm:ss}\t${header.CamelHttpServletRequest?.remoteAddr}\t${header.CamelHttpServletRequest?.method}\t${header.CamelHttpUri}\t${header.endpoint}")
                .transform(body().append('\n'))
                .to("file:logs?fileName=usage_log.txt&fileExist=Append");
    }

}
