package com.im.lac.services.dataset.service;

import com.im.lac.services.util.Utils;
import com.im.lac.dataset.DataItem;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.services.camel.Constants;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;

/**
 *
 * @author timbo
 */
public class DatasetRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(DatasetRouteBuilder.class.getName());

    @Override
    public void configure() throws Exception {
        
        restConfiguration().component("servlet").host("0.0.0.0");

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("/ping")
                .get().description("Simple ping service to check things are running")
                .produces("text/plain")
                .route()
                .transform(constant("Ping\n")).endRest();

        rest("/v1/datasets")
                // POST
                .post()
                .description("Upload file to create new dataset. File is the body and dataset name is given by the header named " + DataItem.HEADER_DATA_ITEM_NAME)
                .bindingMode(RestBindingMode.off)
                .produces("application/json")
                .to("direct:datasets/upload")
                // 
                // DELETE
                .delete("/{item}").description("Deletes the dataset specified by the ID")
                .to("direct:/datasets/delete")
                //
                // GET all 
                .get().description("List all datasets")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(DataItem.class)
                .to("direct:/datasets/list")
                //
                // GET DataItem for one
                .get("/{item}/dataitem").description("Gets a description of the dataset specified by the ID as JSON")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(DataItem.class)
                .to("direct:/datasets/dataitem")
                //
                // GET content for item
                .get("/{item}/content").description("Gets the actual data content specified by the ID as JSON")
                .bindingMode(RestBindingMode.off).produces("application/json")
                .to("direct:/datasets/content");

        /* These are the implementation endpoints - not accessible directly from "outside"
         */
        from("direct:datasets/upload")
                .process((Exchange exchange) -> {
                    String specifiedName = exchange.getIn().getHeader(DataItem.HEADER_DATA_ITEM_NAME, String.class);
                    DataItem created = null;
                    InputStream body = exchange.getIn().getBody(InputStream.class);
                    if (body != null) {
                        DatasetHandler datasetHandler = Utils.getDatasetHandler(exchange);
                        InputStream gunzip = IOUtils.getGunzippedInputStream(body);
                        // TODO - allow this to handle things other than molecules.
                        try (Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(gunzip).getStream(false)) {
                            DataItem result = datasetHandler.createDataset(
                                    mols,
                                    specifiedName == null ? "File uploaded on " + new Date().toString() : specifiedName);
                            if (result != null) {
                                created = result;
                            }
                        }

                    }
                    exchange.getOut().setBody(created);
                })
                .marshal().json(JsonLibrary.Jackson);

        from("direct:/datasets/delete")
                .log("deleting dataset ${header.item}")
                .setBody(header("item")).convertBodyTo(Long.class)
                .beanRef(Constants.DATASET_HANDLER, "deleteDataset")
                .transform(constant("OK"));

        from("direct:/datasets/list")
                .beanRef(Constants.DATASET_HANDLER, "listDataItems");

        from("direct:/datasets/dataitem")
                .setBody(header("item")).convertBodyTo(Long.class)
                .beanRef(Constants.DATASET_HANDLER, "getDataItem");

        from("direct:/datasets/content")
                .setBody(header("item")).convertBodyTo(Long.class)
                .beanRef(Constants.DATASET_HANDLER, "fetchJsonForDataset")
                .setBody(simple("${body.inputStream}"));

    }

}
