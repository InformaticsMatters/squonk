package com.im.lac.service.impl;

import com.im.lac.model.DataItem;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.jobs.impl.CamelExecutor;
import com.im.lac.jobs.impl.DatasetHandler;
import com.im.lac.service.JobService;
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

        /* These are the REST endpoints - exposed as public web services 
         */
        rest("/rest/v1/datasets")
                // POST
                .post()
                .description("Upload file to create new dataset")
                .bindingMode(RestBindingMode.off)
                .produces("application/json")
                .to("direct:datasets/upload")
                // 
                // DELETE
                .delete("/{item}").description("Deletes the dataset specified by the ID")
                .to("direct:/datasets/delete")
                //
                // GET all 
                .get()
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(DataItem.class)
                .to("direct:/datasets/list")
                //
                // GET DataItem for one
                .get("/{item}/dataitem").description("Gets the dataset info specified by the ID as JSON")
                .bindingMode(RestBindingMode.json).produces("application/json")
                .outType(DataItem.class)
                .to("direct:/datasets/dataitem")
                //
                // GET data for item
                .get("/{item}/content").description("Gets the actual data content specified by the ID as JSON")
                .bindingMode(RestBindingMode.off).produces("application/json")
                .to("direct:/datasets/content");

        /* These are the implementation endpoints - not accessible directly from "outside"
         */
        from("direct:datasets/upload")
                .process((Exchange exchange) -> {
                    String specifiedName = exchange.getIn().getHeader(JobService.HEADER_DATA_ITEM_NAME, String.class);
                    DataItem created = null;
                    InputStream body = exchange.getIn().getBody(InputStream.class);
                    if (body != null) {
                        DatasetHandler datasetHandler = CamelExecutor.getDatasetHandler(exchange);
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
                .beanRef(CamelExecutor.DATASET_HANDLER, "deleteDataset")
                .transform(constant("OK"));

        from("direct:/datasets/list")
                .beanRef(CamelExecutor.DATASET_HANDLER, "listDataItems");

        from("direct:/datasets/dataitem")
                .setBody(header("item")).convertBodyTo(Long.class)
                .beanRef(CamelExecutor.DATASET_HANDLER, "getDataItem");

        from("direct:/datasets/content")
                .setBody(header("item")).convertBodyTo(Long.class)
                .beanRef(CamelExecutor.DATASET_HANDLER, "fetchJsonForDataset")
                .setBody(simple("${body.inputStream}"));

    }

}