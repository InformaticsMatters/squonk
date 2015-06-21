package com.im.lac.jobs.impl.files;

import com.im.lac.model.DataItem;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.jobs.impl.CamelExecutor;
import com.im.lac.jobs.impl.DatasetHandler;
import com.im.lac.service.JobService;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.activation.DataHandler;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 *
 * @author timbo
 */
public class FileHandlerRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(FileHandlerRouteBuilder.class.getName());

    @Override
    public void configure() throws Exception {

        from("jetty://http://0.0.0.0:8080/files/upload")
                .log("Uploading structure file")
                //.wireTap("direct:logger")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Map<String, DataHandler> attachements = exchange.getIn().getAttachments();
                    String specifiedName = exchange.getIn().getHeader(JobService.HEADER_DATA_ITEM_NAME, String.class);
                    List<DataItem> created = new ArrayList<>();
                    if (attachements != null) {
                        DatasetHandler datasetHandler = CamelExecutor.getDatasetHandler(exchange);
                        int counter = 0;
                        for (DataHandler dh : attachements.values()) {
                            counter++;
                            InputStream is = dh.getInputStream();
                            InputStream gunzip = IOUtils.getGunzippedInputStream(is);
                            // TODO - allow this to handle things other than molecules.
                            try (Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(gunzip).getStream(false)) {
                                DataItem result = datasetHandler.createDataset(
                                        mols,
                                        generateItemName(specifiedName, dh.getName(), attachements.size(), counter));
                                if (result != null) {
                                    created.add(result);
                                }
                            }
                        }
                    }
                    exchange.getIn().setBody(created);
                })
                .marshal().json(JsonLibrary.Jackson);
    }

    private String generateItemName(String specifiedName, String attachmentName, int totalItems, int currentItem) {
        if (totalItems == 1) {
            return (specifiedName == null ? attachmentName : specifiedName);
        } else {
            return (specifiedName == null ? attachmentName + "_" + currentItem : specifiedName + "_" + currentItem);
        }
    }

}
