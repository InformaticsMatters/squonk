/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.demo.routes;

import com.im.lac.camel.chemaxon.processor.db.JChemDBSearcher;
import com.im.lac.types.MoleculeObject;
import com.squonk.dataset.MoleculeObjectDataset;
import com.squonk.types.SDFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import javax.sql.DataSource;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.postgresql.ds.PGSimpleDataSource;

/**
 *
 * @author timbo
 */
public class EmolsMultiSearchRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:readsdf")
                .convertBodyTo(SDFile.class)
                .convertBodyTo(MoleculeObjectDataset.class);

        from("direct:multisearch")
                //                .process((exch) -> {
                //                    MoleculeObjectDataset mods = exch.getIn().getBody(MoleculeObjectDataset.class);
                //                    exch.getIn().setBody(mods.getItems());
                //                })
                .to("direct:multisearch/emolecules_bb");

    }

    public static void main(String[] args) throws Exception {
        DataSource ds = createDataSource();
//        try (Connection con = ds.getConnection()) {
//            ResultSet rs = con.createStatement().executeQuery("SELECT count(*) FROM vendordbs.emolecules_orderbb");
//            if (rs.next()) {
//                int rows = rs.getInt(1);
//                System.out.println("Found " + rows + " rows");
//            } else {
//                System.out.println("No rows");
//            }
//        }

        DefaultCamelContext context = new DefaultCamelContext();
        context.addRoutes(new EmolsMultiSearchRouteBuilder());
        context.addRoutes(new DatabaseRouteBuilder(ds));
        context.start();

        String file = "../../data/testfiles/Kinase_inhibs.sdf.gz";

        try (InputStream is = new FileInputStream(file)) {

            MoleculeObjectDataset mods = parseSDF(context, is);
            System.out.println(mods);
            List<MoleculeObject> mols = mods.getItems();
            System.out.println("#Results = " + mols.size());

//            MoleculeObjectDataset qresults = searchEmolecules(context, "Cn1ncc2cc(CN)ccc12", "t:ff");
//            List<MoleculeObject> mols = qresults.getItems();
            MoleculeObjectDataset r = searchMulti(context, mols, "t:i");
            System.out.println("Results: " + r);
            System.out.println(r.getItems().size() + " items");

        } finally {
            context.shutdown();
        }
    }

    static MoleculeObjectDataset parseSDF(CamelContext context, InputStream sdf) throws Exception {

        ProducerTemplate pt = context.createProducerTemplate();
        MoleculeObjectDataset o = pt.requestBody("direct:readsdf", sdf, MoleculeObjectDataset.class);
        return o;

    }

    static MoleculeObjectDataset searchEmolecules(CamelContext context, String query, String searchOptions) {
        System.out.println("Searching for " + query);
        ProducerTemplate pt = context.createProducerTemplate();
        MoleculeObjectDataset o = pt.requestBodyAndHeader(
                "direct:chemsearch/emolecules_bb",
                query,
                JChemDBSearcher.HEADER_SEARCH_OPTIONS, searchOptions,
                MoleculeObjectDataset.class);
        System.out.println("Results: " + o);
        return o;
    }

    static MoleculeObjectDataset searchMulti(CamelContext context, List<MoleculeObject> mols, String searchOptions) throws IOException {
        ProducerTemplate pt = context.createProducerTemplate();
        MoleculeObjectDataset o = pt.requestBodyAndHeader(
                "direct:multisearch/emolecules_bb",
                mols,
                JChemDBSearcher.HEADER_SEARCH_OPTIONS, searchOptions,
                MoleculeObjectDataset.class);
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

}
