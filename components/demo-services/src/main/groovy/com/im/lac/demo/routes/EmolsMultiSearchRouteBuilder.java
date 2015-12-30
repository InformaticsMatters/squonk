package com.im.lac.demo.routes;

import chemaxon.struc.Molecule;
import org.squonk.camel.chemaxon.processor.ChemAxonMoleculeProcessor;
import org.squonk.camel.chemaxon.processor.db.JChemDBSearcher;
import org.squonk.camel.dataformat.MoleculeObjectDatasetJsonDataFormat;
import org.squonk.camel.dataformat.SimpleJsonDataFormat;
import org.squonk.camel.processor.StreamingMoleculeObjectSourcer;
import org.squonk.camel.processor.ValueTransformerProcessor;
import org.squonk.camel.util.CamelUtils;
import org.squonk.chemaxon.molecule.MoleculeObjectWriter;
import org.squonk.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.SDFile;
import org.squonk.types.io.JsonHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.postgresql.ds.PGSimpleDataSource;

/**
 *
 * @author timbo
 */
public class EmolsMultiSearchRouteBuilder extends RouteBuilder {

    private static final String PROP_LOGP = "LogP";
    private static final String PROP_MOLWEIGHT = "MolWeight";
    private static final String PROP_HEAVY_ATOM_COUNT = "HeavyAtomCount";

    private static final String TABLE_NAME = "emolecules_order_bb";

    private static final String HEADER_QUERY_MOLECULEOBJECT_ID = "MoleculeObjectID";

    private static final String FIELD_HISTORY = "history";

    String base = "../../lacfiledrop/";
    
     private static final String EMOLS_SEARCHER_ROUTE = "direct:chemsearch/emolecules_all";

    @Override
    public void configure() throws Exception {

        MoleculeObjectDatasetJsonDataFormat modsjdf = new MoleculeObjectDatasetJsonDataFormat();
        SimpleJsonDataFormat dmddf = new SimpleJsonDataFormat(DatasetMetadata.class);

        from("direct:exportmols")
                .process((Exchange exchange) -> {
                    String accept = exchange.getIn().getHeader("Accept", String.class);
                    String options = exchange.getIn().getHeader("MolExporterOptions", String.class);
                    boolean gzip = "gzip".equals(exchange.getIn().getHeader("Accept-Encoding", String.class));

                    MoleculeObjectDataset mods = StreamingMoleculeObjectSourcer.bodyAsMoleculeObjectDataset(exchange);
                    if (mods == null) {
                        Object body = exchange.getIn().getBody();
                        if (body == null) {
                            throw new IllegalStateException("Can't source molecules from body that is null");
                        } else {
                            throw new IllegalStateException("Can't source molecules. Body is of type " + body.getClass().getName());
                        }
                    }
                    MoleculeObjectWriter writer = new MoleculeObjectWriter(mods.getStream());
                    InputStream in = writer.getTextStream(options == null ? "sdf" : options, gzip); // TODO format
                    if (accept == null) {
                        if (options == null) {
                            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "chemical/x-mdl-sdfile");
                        }
                    } else {
                        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, accept);
                    }
                    exchange.getIn().setBody(in);
                });

        from("file:" + base + "combisearch?antInclude=*.json,*.JSON,*.sdf,*.SDF&move=done&moveFailed=error/${file:name}")
                .log("Reading file ${file:name}")
                .process((exch) -> {
                    CamelUtils.putPropertiesAsHeaders(exch.getIn(), new File(base + "combisearch/headers.properties"));
                })
                .to("direct:readFile")
                .convertBodyTo(List.class)
                .split(body(), new MoleculeObjectDeduplicateAggregationStrategy("cd_id"))
                .log("processing search ${header.CamelSplitIndex}")
                .to("direct:prepareForSearch")
                .log("searching")
                .to(EMOLS_SEARCHER_ROUTE)
                .end()
                .log("converting")
                .process((exch) -> {
                    Map<Object, MoleculeObject> items = exch.getIn().getBody(Map.class);
                    System.out.println("Aggregated " + items.size() + " molecules");
                    MoleculeObjectDataset mods = new MoleculeObjectDataset(items.values());
                    exch.getIn().setBody(mods);
                })
                .process((exch) -> {
                    MoleculeObjectDataset mods = exch.getIn().getBody(MoleculeObjectDataset.class);
                    System.out.println("Num items: " + mods.getItems().size());
                })
                .marshal(modsjdf)
                .log("saving")
                .to("file:" + base + "combisearch/output?fileName=${file:name.noext}_Results.json")
                .setBody(header(JsonHandler.ATTR_DATASET_METADATA))
                .marshal(dmddf)
                .to("file:" + base + "combisearch/output?fileName=${file:name.noext}_Results.meta")
                .log("finished");

        from("file:" + base + "multisearch?antInclude=*.json,*.JSON,*.sdf,*.SDF&move=done&moveFailed=error/${file:name}")
                .log("Reading file ${file:name}")
                .process((exch) -> {
                    CamelUtils.putPropertiesAsHeaders(exch.getIn(), new File(base + "multisearch/headers.properties"));
                })
                .to("direct:readFile")
                .convertBodyTo(List.class)
                .split(body())
                .log("processing search ${header.CamelSplitIndex}")
                .to("direct:prepareForSearch")
                .log("searching")
                .to(EMOLS_SEARCHER_ROUTE)
                .process((exch) -> {
                    MoleculeObjectDataset mods = exch.getIn().getBody(MoleculeObjectDataset.class);
                    exch.getIn().setHeader("HitCount", mods.getItems().size());
                })
                .choice()
                .when(header("HitCount").isGreaterThan(0))
                .log("exporting ${header.MoleculeObjectID}")
                .marshal(modsjdf)
                .log("saving")
                .to("file:" + base + "multisearch/output?fileName=${file:name.noext}_${header.MoleculeObjectID}.json")
                .setBody(header(JsonHandler.ATTR_DATASET_METADATA))
                .marshal(dmddf)
                .to("file:" + base + "multisearch/output?fileName=${file:name.noext}_${header.MoleculeObjectID}.meta")
                .otherwise()
                .log("No hits for ${header.MoleculeObjectID}");

        from("direct:prepareForSearch")
                .process((exch) -> {
                    MoleculeObject mo = exch.getIn().getBody(MoleculeObject.class);
                    exch.getIn().setBody(mo.getSource());
                    String idFldName = exch.getIn().getHeader("MoleculeObjectIDFieldName", String.class);
                    if (idFldName != null) {
                        Object v = mo.getValue(idFldName);
                        exch.getIn().setHeader(HEADER_QUERY_MOLECULEOBJECT_ID, v == null ? mo.getUUID().toString() : v);
                    } else {
                        exch.getIn().setHeader(HEADER_QUERY_MOLECULEOBJECT_ID, mo.getUUID().toString());
                    }
                });

        from("file:" + base + "calculateProperties?antInclude=*.json,*.JSON,*.sdf,*.SDF&move=done&moveFailed=error/${file:name}")
                .to("direct:readFile")
                .to("direct:calculateProperties")
                .marshal(modsjdf)
                .to("file:" + base + "calculateProperties/output?fileName=${file:name.noext}.json")
                .setBody(header(JsonHandler.ATTR_DATASET_METADATA))
                .marshal(dmddf)
                .to("file:" + base + "calculateProperties/output?fileName=${file:name.noext}.meta")
                .log("finished");

        from("direct:calculateProperties")
                .convertBodyTo(Dataset.class)
                .log("calculating")
                .process(new ChemAxonMoleculeProcessor()
                        .calculate(PROP_LOGP, "logP()")
                        .calculate(PROP_MOLWEIGHT, "mass()")
                        .calculate(PROP_HEAVY_ATOM_COUNT, "atomCount() - atomCount('1')")
                );

        from("file:" + base + "sdfExport?antInclude=*.json&move=done&moveFailed=error/${file:name}")
                .to("direct:readFile")
                .log("exporting")
                .to("direct:exportmols")
                .log("saving")
                .to("file:" + base + "sdfExport/output?fileName=${file:name.noext}.sdf")
                .log("finished");

        from("file:" + base + "readFile?antInclude=*.json,*.sdf")
                .to("direct:readFile");

        from("direct:readFile")
                .choice()
                .when(simple("${file:ext} == 'sdf' || ${file:ext} == 'SDF'"))
                .log("File is SDF")
                .to("direct:readsdf")
                .when(simple("${file:ext} == 'json' || ${file:ext} == 'JSON'"))
                .log("File is JSON")
                .to("direct:readjson")
                .otherwise()
                .log("File is something else")
                .throwException(new IllegalStateException("Body is not SDF or JSON"));

        from("direct:readsdf")
                .convertBodyTo(InputStream.class)
                .convertBodyTo(SDFile.class)
                .convertBodyTo(MoleculeObjectDataset.class);

        from("direct:readjson")
                .process((exch) -> {
                    String jsonfile = exch.getIn().getHeader("CamelFileAbsolutePath", String.class);
                    String metafile = jsonfile.replaceFirst("\\.[Jj][Ss][Oo][Nn]$", ".meta");
                    File meta = new File(metafile);
                    if (meta.exists()) {
                        DatasetMetadata dmd = (DatasetMetadata) dmddf.unmarshal(exch, new FileInputStream(meta));
                        exch.getIn().setHeader(JsonHandler.ATTR_DATASET_METADATA, dmd);
                        // this is not ideal
                        if (!meta.delete()) {
                            System.out.println("Failed to delete metadata");
                        }
                    } else {
                        System.out.println("No metadata found");
                    }
                })
                .process((exch) -> { // we can't used .unmarshal() directly as it closes the InputStream                   
                    InputStream is = exch.getIn().getBody(InputStream.class);
                    MoleculeObjectDataset mods = (MoleculeObjectDataset) modsjdf.unmarshal(exch, is);
                    exch.getIn().setBody(mods);
                });

        from("direct:convertValues")
                .process(new ValueTransformerProcessor()
                        .convertValueType("LogP", Float.class)
                        .convertValueName("LogP", "clogp"));

    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running EmolsMultiSearchRouteBuilder");
        DataSource ds = createDataSource();

        DefaultCamelContext context = new DefaultCamelContext();
        context.addRoutes(new EmolsMultiSearchRouteBuilder());
        context.addRoutes(new DatabaseRouteBuilder(ds));
        startAndWait(context);

    }

    static void startAndWait(CamelContext camelContext) throws InterruptedException {


        System.out.println("Starting CamelContext");
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    camelContext.start();
                } catch (Exception ex) {
                    System.out.println("Failed to start Camel");
                    ex.printStackTrace();
                }
            }
        };
        t.start();
        System.out.println("CamelContext started");

        Thread.currentThread().join();

        System.out.println("Should never get here");
    }

    static String exportStructures(CamelContext context, Stream<MoleculeObject> mols, String mimeType, String options) {
        ProducerTemplate pt = context.createProducerTemplate();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Accept", mimeType);
        headers.put("MolExporterOptions", options);
        String is = pt.requestBodyAndHeaders("direct:exportmols", mols, headers, String.class
        );
        return is;
    }

    static String generateQueryStringWithMWFilter(MoleculeObject mo, float range) {
        float mw = mo.getValue(PROP_MOLWEIGHT, Float.class
        );
        float minMw = mw - range;
        float maxMw = mw + range;

        return "sep=# t:ff#filterQuery:SELECT cd_id from " + TABLE_NAME + " WHERE cd_molweight < " + maxMw
                + " AND cd_molweight > " + minMw;
    }

    static MoleculeObjectDataset parseSDF(CamelContext context, InputStream sdf) throws Exception {
        ProducerTemplate pt = context.createProducerTemplate();
        MoleculeObjectDataset o = pt.requestBody("direct:readsdf", sdf, MoleculeObjectDataset.class
        );
        return o;
    }

    static Stream<MoleculeObject> calcMolweight(CamelContext context, Stream<MoleculeObject> mols) {
        return mols.peek((mo) -> {
            Molecule mol = MoleculeUtils.fetchMolecule(mo, true);
            mo.putValue(PROP_MOLWEIGHT, mol.getMass());
        });
    }

    Stream<MoleculeObject> calcHeavyAtomCount(CamelContext context, Stream<MoleculeObject> mols) {
        return mols.peek((mo) -> {
            Molecule mol = MoleculeUtils.fetchMolecule(mo, true);
            mo.putValue(PROP_HEAVY_ATOM_COUNT, MoleculeUtils.heavyAtomCount(mol));
        });
    }

    static MoleculeObjectDataset searchEmolecules(CamelContext context, String query, String searchOptions) {
        System.out.println("Searching for " + query);
        ProducerTemplate pt = context.createProducerTemplate();
        MoleculeObjectDataset o = pt.requestBodyAndHeader(
                EMOLS_SEARCHER_ROUTE,
                query,
                JChemDBSearcher.HEADER_SEARCH_OPTIONS, searchOptions,
                MoleculeObjectDataset.class
        );
        System.out.println(
                "Results: " + o);
        return o;
    }

    static Stream<MoleculeObject> searchEmolecules(CamelContext context, Stream<MoleculeObject> queries, String searchOptions) {

        return queries.sequential().peek((mo) -> {
            System.out.println("Running search ...");
            MoleculeObjectDataset o = searchEmolecules(context, mo.getSource(), searchOptions);
            System.out.println("Results: " + o);
        });
    }

    static MoleculeObjectDataset searchMulti(CamelContext context, List<MoleculeObject> mols, String searchOptions) throws IOException {
        ProducerTemplate pt = context.createProducerTemplate();
        MoleculeObjectDataset o = pt.requestBodyAndHeader(
                "direct:multisearch/emolecules_bb",
                mols,
                JChemDBSearcher.HEADER_SEARCH_OPTIONS, searchOptions,
                MoleculeObjectDataset.class
        );
        return o;
    }

    public static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerName("localhost");
        ds.setPortNumber(5432);
        ds.setDatabaseName("chemcentral");
        ds.setUser("chemcentral");
        ds.setPassword("chemcentral");
        return ds;
    }

    class MoleculeObjectDeduplicateAggregationStrategy implements AggregationStrategy {

        String key;

        MoleculeObjectDeduplicateAggregationStrategy() {

        }

        MoleculeObjectDeduplicateAggregationStrategy(String key) {
            this.key = key;
        }

        @Override
        public Exchange aggregate(Exchange old, Exchange neu) {

            Map<Object, MoleculeObject> items = (old == null ? new HashMap<>() : old.getIn().getBody(Map.class));
            try {
                MoleculeObjectDataset mods = neu.getIn().getBody(MoleculeObjectDataset.class);
                Object queryid = neu.getIn().getHeader(HEADER_QUERY_MOLECULEOBJECT_ID);
                List<MoleculeObject> mols = mods.getItems();
                long count = mols.stream().peek((mo) -> {
                    Double simscore = mo.getValue("similarity", Double.class);
                    Object value = (key == null ? mo.getUUID(): mo.getValue(key));
                    MoleculeObject retained = items.get(value);
                    if (retained == null) {
                        items.put(value, mo);
                        retained = mo;
                        mo.getValues().remove("similarity");
                    }
                    if (queryid != null && simscore != null) {
                        String line = simscore + " similarity to " + queryid;
                        String history = retained.getValue(FIELD_HISTORY, String.class);
                        history = (history == null ? line : history + "\n" + line);
                        retained.putValue(FIELD_HISTORY, history);
                        retained.putValue("similarity_" + queryid, simscore);
                    }
                }).count();

                neu.getIn().setBody(items);
                return neu;
            } catch (IOException ex) {
                throw new RuntimeException("Unable to read molecules", ex);
            }
        }

    }

}
