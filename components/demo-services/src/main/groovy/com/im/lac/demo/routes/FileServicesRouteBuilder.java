package com.im.lac.demo.routes;

import com.im.lac.camel.dataformat.MoleculeObjectJsonConverter;
import com.im.lac.camel.processor.MoleculeObjectSourcer;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeObjectWriter;
import com.im.lac.demo.services.DbFileService;
import com.im.lac.demo.model.*;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import com.im.lac.util.SimpleStreamProvider;
import com.im.lac.util.StreamProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.activation.DataHandler;
import javax.sql.DataSource;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;

/**
 *
 * @author Tim Dudgeon
 */
public class FileServicesRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(FileServicesRouteBuilder.class.getName());

    private DataSource dataSource;
    private final DbFileService service;

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

        from("jetty://http://0.0.0.0:8080/chemsearch")
                .log("Processing query")
                .wireTap("direct:logger")
                .transform(header("QueryStructure"))
                .log("Routing query to ${headers.endpoint}")
                .routingSlip(header("endpoint"))
                .process((Exchange exchange) -> {
                    createDataItems(exchange);
                })
                .marshal().json(JsonLibrary.Jackson)
                .log("Query response sent");

        from("jetty://http://0.0.0.0:8080/process")
                .log("Processing ...")
                .wireTap("direct:logger")
                //.to("direct:/dump/exchange"
                .process((Exchange exchange) -> {
                    Stream<MoleculeObject> stream = createMoleculeObjectStreamForDataItem(exchange);
                    exchange.getIn().setBody(new SimpleStreamProvider(stream, MoleculeObject.class));
                })
                // send the Molecules to the desired endpoint
                .routingSlip(header("endpoint"))
                // save the molecules as a new DataItem
                .process((Exchange exchange) -> {
                    createDataItems(exchange);
                })
                .marshal().json(JsonLibrary.Jackson)
                .log("Response sent");

        from("jetty://http://0.0.0.0:8080/react")
                .log("Processing ...")
                .wireTap("direct:logger")
                .to("direct:/dump/exchange")
                .to("direct:reactor")
                .process((Exchange exchange) -> {
                    Stream<MoleculeObject> stream = exchange.getIn().getBody(StreamProvider.class).getStream();
                    exchange.getIn().setBody(stream.limit(5000));
                    createDataItems(exchange);
                })
                .marshal().json(JsonLibrary.Jackson)
                .log("Response sent");

        from("jetty://http://0.0.0.0:8080/react_filter_predict")
                .log("Processing ...")
                .wireTap("direct:logger")
                .to("direct:/dump/exchange")
                .to("direct:reactor")
                .to("direct:filter_example")
                .to("direct:lipinski")
                .process((Exchange exchange) -> {
                    Stream<MoleculeObject> stream = exchange.getIn().getBody(StreamProvider.class).getStream();
                    exchange.getIn().setBody(stream.limit(5000));
                    createDataItems(exchange);
                })
                .marshal().json(JsonLibrary.Jackson)
                .log("Response sent");

        from("jetty://http://0.0.0.0:8080/files/upload")
                .log("Uploading file")
                .wireTap("direct:logger")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Map<String, DataHandler> attachements = exchange.getIn().getAttachments();
                    List<DataItem> created = new ArrayList<>();
                    if (attachements != null) {
                        for (DataHandler dh : attachements.values()) {
                            InputStream is = dh.getInputStream();
                            InputStream gunzip = IOUtils.getGunzippedInputStream(is);
                            try (Stream<MoleculeObject> mols = MoleculeObjectUtils.createStreamGenerator(gunzip).getStream(false)) {
                                DataItem result = createDataItem(mols.iterator(), dh.getName());
                                if (result != null) {
                                    created.add(result);
                                }
                            }
                        }
                    }
                    exchange.getIn().setBody(created);
                })
                .marshal().json(JsonLibrary.Jackson);

        rest("/rest/logs")
                .get()
                .route()
                .process((Exchange exchng) -> {
                    exchng.getIn().setBody(new File("logs/usage_log.txt"));
                });

        rest("/rest/files")
                .get()
                .bindingMode(RestBindingMode.json)
                .outType(DataItem.class)
                .route()
                .wireTap("direct:logger")
                .to("direct:/files/list")
                .endRest()
                .delete("/{item}")
                .route()
                .wireTap("direct:logger")
                .to("direct:/files/delete")
                .endRest()
                .get("/{item}")
                .route()
                .wireTap("direct:logger")
                .to("direct:/files/get")
                .endRest()
                .get("/json/{item}")
                .route()
                .wireTap("direct:logger")
                .to("direct:/files/json/get")
                .endRest();

        from("direct:/files/list")
                .process((Exchange exchange) -> {
                    exchange.getIn().setBody(service.loadDataItems());
                });

        from("direct:/files/delete")
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

        from("direct:/files/get")
                .log("Getting file ${headers.item}")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Stream<MoleculeObject> mols = createMoleculeObjectStreamForDataItem(exchange);
                    MoleculeObjectWriter writer = new MoleculeObjectWriter(mols);
                    InputStream out = writer.getTextStream("sdf"); // TODO format
                    exchange.getIn().setBody(out);
                }
                );

        /* this is just for testing/debugging 
         would be better to handle in the files/get route using the mime type that was requested 
         */
        from("direct:/files/json/get")
                .log("Getting file ${headers.item}")
                //.to("direct:/dump/exchange")
                .process((Exchange exchange) -> {
                    Stream<MoleculeObject> stream = createMoleculeObjectStreamForDataItem(exchange);
                    Iterator<MoleculeObject> mols = stream.iterator();
                    MoleculeObjectJsonConverter dataFormat = new MoleculeObjectJsonConverter();
                    InputStream in = createJsonInputStream(mols, dataFormat);
                    exchange.getIn().setBody(in);
                }
                );

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

    // these methods could be moved out to a java bean to simplify things
    //
    protected void createDataItems(Exchange exchange) throws IOException, SQLException {

        Iterator<MoleculeObject> mols = MoleculeObjectSourcer.bodyAsMoleculeObjectIterator(exchange);

        String newName = exchange.getIn().getHeader("itemName", String.class);
        Connection con = service.getConnection();
        boolean ac = con.getAutoCommit();
        if (ac) {
            con.setAutoCommit(false);
        }
        List<DataItem> created = new ArrayList<>();
        try {
            DataItem result = createDataItem(con, mols, newName);
            if (result != null) {
                created.add(result);
                LOG.log(Level.INFO, " Result: ID = {0} Name = {1} LOID = {2}",
                        new Object[]{result.getId(), result.getName(), result.getLoid()});
            }
            con.commit();
        } catch (IOException | SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(ac);
        }
        exchange.getIn().setBody(created);
    }

    protected DataItem createDataItem(Iterator<MoleculeObject> mols, String name) throws IOException, SQLException {
        Connection con = service.getConnection();
        boolean ac = con.getAutoCommit();
        if (ac) {
            con.setAutoCommit(false);
        }
        try {
            return createDataItem(con, mols, name);
        } catch (IOException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(ac);
            try {
                con.close();
            } catch (SQLException se) {
                LOG.log(Level.SEVERE, "Failed to close connection", se);
            }
        }
    }

    protected InputStream createJsonInputStream(final Iterator<MoleculeObject> mols, final MoleculeObjectJsonConverter dataFormat) throws IOException {
        final PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(pis);
        Thread t = new Thread(() -> {
            try {
                dataFormat.marshal(mols, out);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to write MoleculeObjects", ex);
            }
        });
        t.start();
        return pis;
    }

    protected DataItem createDataItem(Connection con, Iterator<MoleculeObject> mols, String name) throws IOException {

        long t0 = System.currentTimeMillis();

        DataItem item = new DataItem();
        item.setName(name);
        item.setSize(0); // this will be updated later
        DataItem result;

        MoleculeObjectJsonConverter dataFormat = new MoleculeObjectJsonConverter();
        final InputStream pis = createJsonInputStream(mols, dataFormat);

        result = service.addDataItem(con, item, pis);
        long t1 = System.currentTimeMillis();
        LOG.log(Level.INFO, "Writing data took {0}ms", (t1 - t0));
        int count = dataFormat.getMarshalCount();
        if (count == 0) {
            LOG.info("No results found");
            service.deleteDataItem(con, result);
            return null;
        } else {
            result.setSize(count);
            if (result.getName() == null) {
                result.setName("DataItem " + result.getId());
            }
            LOG.log(Level.INFO, "Updating Item: ID={0} Name={1} Size={2} LOID={3}",
                    new Object[]{result.getId(), result.getName(), result.getSize(), result.getLoid()});
            return service.updateDataItem(con, result);
        }
    }

    private Stream<MoleculeObject> createMoleculeObjectStreamForDataItem(Exchange exchange) throws SQLException, IOException {
        // 1. grab item id from header and load its DataItem from the service
        Long dataId = exchange.getIn().getHeader("item", Long.class);
        DataItem sourceData = service.loadDataItem(dataId);
        LOG.log(Level.INFO, " Source: Data ID = {0} | ID = {1} Name = {2} LOID = {3}",
                new Object[]{dataId, sourceData.getId(), sourceData.getName(), sourceData.getLoid()});

        // 2. create a Steam<MoleculeObject> from the InputStream of the item's large object
        Connection con = service.getConnection();
        boolean ac = con.getAutoCommit();
        if (ac) {
            con.setAutoCommit(false);
        }

        final InputStream input = service.createLargeObjectReader(con, sourceData.getLoid());
        return createMoleculeObjectStreamFromJson(input);
    }

    private Stream<MoleculeObject> createMoleculeObjectStreamFromJson(InputStream is) throws IOException {
        InputStream gunzip = IOUtils.getGunzippedInputStream(is);
        final MoleculeObjectJsonConverter dataFormat = new MoleculeObjectJsonConverter();
        Stream<MoleculeObject> stream = dataFormat.unmarshal(gunzip);
        return stream;
    }

}
