package com.im.lac.camel.chemaxon.routes;

import chemaxon.formats.MolExporter;
import chemaxon.marvin.io.MolExportException;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.io.MoleculeIOUtils;
import dataFormat.MoleculeIteratorDataFormat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

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

        from("direct:handleAtomCount")
                .to("direct:atomcount")
                .log("Calculations complete")
                .marshal(molDataFormat)
                .log("Marshalling complete");

        from("direct:tryit")
                .split().method(MoleculeIOUtils.class, "moleculeIterator").streaming()
                .log("${body}")
                .end()
                .transform(constant("Job done\n"));

    }
}
