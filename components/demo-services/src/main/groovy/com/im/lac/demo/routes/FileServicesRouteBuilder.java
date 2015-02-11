package com.im.lac.demo.routes;

import com.im.lac.camel.dataformat.MoleculeObjectDataFormat;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectWriter;
import com.im.lac.demo.services.DbFileService;
import com.im.lac.demo.model.*;
import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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
                .log("Processing file")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Long dataId = exchange.getIn().getHeader("item", Long.class);
                    String endpoint = exchange.getIn().getHeader("endpoint", String.class);
                    String newName = exchange.getIn().getHeader("itemName", String.class);
                    DataItem sourceData = service.loadDataItem(dataId);
                    LOG.log(Level.INFO, " Source: Data ID = {0} | ID = {1} Name = {2} LOID = {3}",
                            new Object[]{dataId, sourceData.getId(), sourceData.getName(), sourceData.getLoid()});
                    InputStream input = service.createLargeObjectReader(sourceData.getLoid());
                    MoleculeObjectIterable moit = createMoleculeObjectIterable(input);
                    try {
                        ProducerTemplate t = exchange.getContext().createProducerTemplate();
                        exchange.getIn().setBody(moit);
                        Exchange exchResult = t.send(endpoint, exchange);
                        if (exchResult.getException() != null) {
                            throw exchResult.getException();
                        }

                        MoleculeObjectIterable mols = exchResult.getIn().getBody(MoleculeObjectIterable.class);
                        DataItem result = createDataItem(mols, newName);
                        List<DataItem> created = new ArrayList<>();
                        if (result != null) {
                            created.add(result);
                            LOG.log(Level.INFO, " Result: Data ID = {0} | ID = {1} Name = {2} LOID = {3}",
                                    new Object[]{dataId, result.getId(), result.getName(), result.getLoid()});

                        }
                        exchange.getOut().setBody(created);
                    } finally {
                        IOHelper.close(input);
                    }
                })
                .marshal().json(JsonLibrary.Jackson)
                .log("Response sent");

        from("jetty://http://0.0.0.0:8080/files/upload")
                .log("Uploading file")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Map<String, DataHandler> attachements = exchange.getIn().getAttachments();
                    List<DataItem> created = new ArrayList<>();
                    if (attachements != null) {
                        for (DataHandler dh : attachements.values()) {
                            InputStream is = dh.getInputStream();
                            InputStream gunzip = IOUtils.getGunzippedInputStream(is);
                            MoleculeObjectIterable mols = MoleculeObjectUtils.createIterable(gunzip);
                            DataItem result = createDataItem(mols, dh.getName());
                            if (result != null) {
                                created.add(result);
                            }
                        }
                    }
                    exchange.getIn().setBody(created);
                })
                .marshal().json(JsonLibrary.Jackson);

        from("jetty://http://0.0.0.0:8080/files/download")
                .log("Downloading file")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Long dataId = exchange.getIn().getHeader("item", Long.class);
                    DataItem data = service.loadDataItem(dataId);
                    if (data == null) {
                        exchange.getIn().setBody(null);
                        throw new IllegalArgumentException("Item ID " + dataId + " not found");
                    }
                    InputStream input = service.createLargeObjectReader(data.getLoid());
                    MoleculeObjectIterable mols = createMoleculeObjectIterable(input);
                    MoleculeObjectWriter writer = new MoleculeObjectWriter(mols);
                    InputStream out = writer.getTextStream("sdf"); // TODO format
                    exchange.getIn().setBody(out);
                });

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

//    protected DataItem createDataItem(MoleculeObjectIterable mols, String name) throws IOException {
//
//        long t0 = System.currentTimeMillis();
//        MoleculeObjectWriter writer = new MoleculeObjectWriter(mols);
//        InputStream out = writer.getTextStream("sdf"); // TODO format
//        DataItem item = new DataItem();
//        item.setName(name);
//        item.setSize(0); // this will be updated later
//        DataItem result;
//        result = service.addDataItem(item, out);
//        long t1 = System.currentTimeMillis();
//        LOG.log(Level.INFO, "Writing data took {0}ms", (t1-t0));
//        int count = writer.getMarshalCount();
//        if (count == 0) {
//            LOG.info("No results found");
//            service.deleteDataItem(result);
//            return null;
//        } else {
//            result.setSize(count);
//            LOG.log(Level.INFO, "Updating Item: ID={0} Name={1} Size={2} LOID={3}",
//                    new Object[]{result.getId(), result.getName(), result.getSize(), result.getLoid()});
//            return service.updateDataItem(result);
//        }
//    }
    protected DataItem createDataItem(MoleculeObjectIterable mols, String name) throws IOException {

        long t0 = System.currentTimeMillis();

        DataItem item = new DataItem();
        item.setName(name);
        item.setSize(0); // this will be updated later
        DataItem result;

        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(pis);
        final MoleculeObjectDataFormat modf = new MoleculeObjectDataFormat();
        Thread t = new Thread(
                () -> {
                    try {
                        modf.marshal(mols, out);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to write MolecuelObjects", ex);
                    }
                });
        t.start();

        result = service.addDataItem(item, pis);
        long t1 = System.currentTimeMillis();
        LOG.log(Level.INFO, "Writing data took {0}ms", (t1 - t0));
        int count = modf.getMarshalCount();
        if (count == 0) {
            LOG.info("No results found");
            service.deleteDataItem(result);
            return null;
        } else {
            result.setSize(count);
            if (result.getName() == null) {
                result.setName("DataItem " + result.getId());
            }
            LOG.log(Level.INFO, "Updating Item: ID={0} Name={1} Size={2} LOID={3}",
                    new Object[]{result.getId(), result.getName(), result.getSize(), result.getLoid()});
            return service.updateDataItem(result);
        }
    }

    private MoleculeObjectIterable createMoleculeObjectIterable(InputStream is) throws IOException {
        InputStream gunzip = IOUtils.getGunzippedInputStream(is);
        final MoleculeObjectDataFormat modf = new MoleculeObjectDataFormat();
        return (MoleculeObjectIterable) modf.unmarshal(gunzip);
    }
}
