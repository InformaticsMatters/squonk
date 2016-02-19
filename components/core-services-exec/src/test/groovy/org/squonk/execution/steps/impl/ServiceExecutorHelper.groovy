package org.squonk.execution.steps.impl

import com.im.lac.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.types.io.JsonHandler
import org.squonk.util.IOUtils

import java.util.stream.Stream

/**
 * Created by timbo on 18/02/16.
 */
class ServiceExecutorHelper {

    static def mols = [
            new MoleculeObject("C", "smiles", [num: "1", hello: 'world']),
            new MoleculeObject("CC", "smiles", [num: "99", hello: 'mars']),
            new MoleculeObject("CCC", "smiles", [num: "100", hello: 'mum'])
    ]

    static CamelContext createCamelContext() {
        DefaultCamelContext context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {
            void configure() {

                restConfiguration().component("jetty").host("0.0.0.0").port(8888);

                rest("/route1").post().route().process() { exch ->
                    handle(exch, "route1", 99)
                }

                rest("/route2").post().route().process() { exch ->
                    handle(exch, "route2", 88)
                }
            }

            void handle(Exchange exch, String prop, Object value) {
                InputStream is = exch.in.getBody(InputStream.class)
                Dataset ds = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), IOUtils.getGunzippedInputStream(is))
                Stream s = ds.stream.peek() { it.putValue(prop, value) }
                InputStream out = JsonHandler.getInstance().marshalStreamToJsonArray(s, false)
                exch.in.body = out
            }
        })

        return context
    }

}
