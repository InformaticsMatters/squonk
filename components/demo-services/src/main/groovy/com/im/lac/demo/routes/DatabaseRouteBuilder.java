package com.im.lac.demo.routes;

import com.im.lac.camel.processor.HeaderPropertySetterProcessor;
import com.im.lac.camel.chemaxon.processor.db.JChemDBSearcher;
import com.im.lac.types.MoleculeObject;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;

/**
 *
 * @author Tim Dudgeon
 */
public class DatabaseRouteBuilder extends RouteBuilder {

    private DataSource dataSource;

    public DatabaseRouteBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
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

        // structure searche for drugbank
        from("direct:chemsearch/drugbank")
                .convertBodyTo(String.class)
                .log("Searching DrugBank")
                .process(new JChemDBSearcher()
                        .dataSource(dataSource)
                        .structureTable("vendordbs.drugbank_feb_2014")
                        .propertyTable("vendordbs.jchemproperties")
                        .searchOptions("t:d")
                        .outputColumn("drugbank_id")
                        .outputColumn("generic_name")
                        .outputMode(JChemDBSearcher.OutputMode.MOLECULE_OBJECTS)
                        .structureFormat("mol")
                );

        // structure search for eMolecules screening compounds
        from("direct:chemsearch/emolecules_sc")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                        .dataSource(dataSource)
                        .structureTable("vendordbs.emolecules_order__sc")
                        .propertyTable("vendordbs.jchemproperties")
                        .searchOptions("t:d")
                        .searchOptionsOverride("maxResults:5000 maxTime:30000")
                        .outputColumn("version_id")
                        .outputMode(JChemDBSearcher.OutputMode.MOLECULE_OBJECTS)
                        .structureFormat("smiles:-a")
                );

        from("direct:chemsearch/emolecules_bb")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                        .dataSource(dataSource)
                        .structureTable("vendordbs.emolecules_order_bb")
                        .propertyTable("vendordbs.jchemproperties")
                        .searchOptions("t:d")
                        .searchOptionsOverride("maxResults:5000 maxTime:30000")
                        .outputColumn("cd_id").outputColumn("version_id")
                        .outputMode(JChemDBSearcher.OutputMode.MOLECULE_OBJECTS)
                        .structureFormat("smiles")
                );
        
        from("direct:chemsearch/emolecules_all")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                        .dataSource(dataSource)
                        .structureTable("vendordbs.emolecules_order_all")
                        .propertyTable("vendordbs.jchemproperties")
                        .searchOptions("t:d")
                        .searchOptionsOverride("maxResults:5000 maxTime:30000")
                        .outputColumn("cd_id").outputColumn("version_id")
                        .outputMode(JChemDBSearcher.OutputMode.MOLECULE_OBJECTS)
                        .structureFormat("smiles")
                );

        from("direct:multisearch/emolecules_bb")
                .convertBodyTo(List.class)
                .setHeader(JChemDBSearcher.HEADER_OUTPUT_MODE, constant(JChemDBSearcher.OutputMode.CD_IDS))
                .split(body(), new DeduplicateAggregationStrategy()).streaming()
                .process((exch) -> {
                    MoleculeObject mo = exch.getIn().getBody(MoleculeObject.class);
                    exch.getIn().setBody(mo.getSource());
                })
                //.log("Searching for ${body}")
                .to("direct:chemsearch/emolecules_bb")
                .log("Found hits: ${body}")
                .end()
                .log("Total hits: ${body}" );

        // filedrop service for searching eMolecules screening compounds
        String emolsbase = "../../lacfiledrop/dbsearch/emolecules_sc";
        from("file:" + emolsbase + "?antInclude=*.mol&preMove=processing&move=../in")
                .process(new HeaderPropertySetterProcessor(new File(emolsbase + "/headers.properties")))
                .to("direct:chemsearch/emolecules_sc")
                .convertBodyTo(InputStream.class)
                .to("file:" + emolsbase + "/out?fileName=${file:name.noext}.sdf");

        // filedrop service for searching drugbank
        String dbbase = "../../lacfiledrop/dbsearch/drugbank";
        from("file:" + dbbase + "?antInclude=*.mol&preMove=processing&move=../in")
                .process(new HeaderPropertySetterProcessor(new File(dbbase + "/headers.properties")))
                .to("direct:chemsearch/drugbank")
                .convertBodyTo(InputStream.class)
                .to("file:" + dbbase + "/out?fileName=${file:name.noext}.sdf");

    }

    class DeduplicateAggregationStrategy implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange old, Exchange neu) {
            Set items;
            if (old == null) {
                items = new HashSet();
            } else {
                items = old.getIn().getBody(Set.class);
            }
            List i = neu.getIn().getBody(List.class);
            if (i != null) {
                items.addAll(i);
            }
            neu.getIn().setBody(items);
            return neu;
        }

    }

}
