package com.im.lac.demo.routes;

import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectWriter;
import com.im.lac.util.OutputGenerator;
import com.im.lac.demo.services.DbFileService;
import com.im.lac.demo.model.*;
import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.IOUtils;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.sql.DataSource;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.util.IOHelper;

/**
 *
 * @author Tim Dudgeon
 */
public class FileServicesRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(FileServicesRouteBuilder.class.getName());

    private DataSource dataSource;
    private DbFileService service;

    public FileServicesRouteBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
        this.service = new DbFileService(dataSource);
    }

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void configure() throws Exception {

        from("jetty://http://0.0.0.0:8080/debug")
                .log("Debug params")
                .to("direct:/dump/exchange");

        from("jetty://http://0.0.0.0:8080/process")
                .log("Processing")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Long dataId = exchange.getIn().getHeader("item", Long.class);
                    String endpoint = exchange.getIn().getHeader("endpoint", String.class);
                    DataItem data = service.loadDataItem(dataId);
                    LOG.log(Level.INFO, "ID = {0} Data ID = {1} Data LOID = {2}", new Object[]{dataId, data.getId(), data.getLoid()});
                    InputStream input = service.createLargeObjectReader(data.getLoid());
                    InputStream gunzip = IOUtils.getGunzippedInputStream(input);
                    InputStream output = null;
                    try {
                        ProducerTemplate t = exchange.getContext().createProducerTemplate();
                        OutputGenerator body = t.requestBody(endpoint, gunzip, OutputGenerator.class);
                        LOG.log(Level.INFO, "Got output: {0}", body);
                        output = body.getTextStream("sdf");
                        DataItem result = service.addDataItem(data, output);
                        LOG.log(Level.INFO, "Got result from processing: {0}", result);
                    } finally {
                        IOHelper.close(input);
                        if (output != null) {
                            IOHelper.close(output);
                        }
                    }

                    exchange.getIn().setBody(data);
                })
                .marshal().json(JsonLibrary.Jackson);

        from("jetty://http://0.0.0.0:8080/files/upload")
                .log("Uploading file")
                .to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Map<String, DataHandler> attachements = exchange.getIn().getAttachments();
                    List<DataItem> created = new ArrayList<>();
                    if (attachements != null) {
                        for (DataHandler dh : attachements.values()) {
                            InputStream is = dh.getInputStream();
                            InputStream gunzip = IOUtils.getGunzippedInputStream(is);

                            MoleculeObjectIterable mols = MoleculeObjectUtils.createIterable(gunzip);
                            MoleculeObjectWriter writer = new MoleculeObjectWriter(mols);
                            InputStream out = writer.getTextStream("sdf"); // TODO format
                            DataItem item = new DataItem();
                            item.setName(dh.getName());
                            item.setSize(0); // this will be updated later
                            DataItem result;
                            try {
                                result = service.addDataItem(item, out);
                                result.setSize(writer.getCount());
                            } finally {
                                IOHelper.close(is);
                            }
                            DataItem finalResult = service.updateDataItem(result);
                            created.add(finalResult);
                        }
                    }
                    exchange.getIn().setBody(created);
                })
                .marshal().json(JsonLibrary.Jackson);

        from("jetty://http://0.0.0.0:8080/files/delete")
                .log("deleting file")
                .process((Exchange exchange) -> {
                    Long dataId = exchange.getIn().getHeader("item", Long.class);
                    DataItem data = service.loadDataItem(dataId);
                    if (data != null) {
                        service.deleteDataItem(data);
                    } else {
                        LOG.log(Level.INFO, "Data item with ID {0} not found", dataId);
                    }
                    exchange.getIn().setBody(null);
                });

        rest("/rest/files/list")
                .bindingMode(RestBindingMode.json)
                .get()
                .outType(DataItem.class)
                .route()
                .wireTap("direct:logger")
                .to("direct:/files/list");

        from("direct:/files/list")
                .process((Exchange exchange) -> {
                    exchange.getIn().setBody(service.loadDataItems());
                });

        from("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    StringBuilder b = new StringBuilder("Exchange info\n");
                    Object body = exchange.getIn().getBody();
                    if (body != null) {
                        b.append("body: ").append(body.getClass().getName());
                    }

                    b.append("\n------ In Message Headers ------\n");
                    Map<String, Object> headers = exchange.getIn().getHeaders();
                    for (Map.Entry<String, Object> e : headers.entrySet()) {
                        b.append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
                    }
                    b.append("------ In Message Attachements ------\n");
                    Map<String, DataHandler> attachements = exchange.getIn().getAttachments();
                    for (Map.Entry<String, DataHandler> e : attachements.entrySet()) {
                        b.append(e.getKey()).append(" -> ").append(e.getValue().getName()).append("\n");
                    }

                    LOG.info(b.toString());
                });

    }

}
