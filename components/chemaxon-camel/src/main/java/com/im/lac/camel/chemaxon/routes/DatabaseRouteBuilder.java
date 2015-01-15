package com.im.lac.camel.chemaxon.routes;

import com.im.lac.camel.chemaxon.processor.HeaderPropertySetterProcessor;
import com.im.lac.camel.chemaxon.processor.db.JChemDBSearcher;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

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


        from("direct:chemsearch/drugbank")
                .convertBodyTo(String.class)
                .log("Searching DrugBank")
                .process(new JChemDBSearcher()
                        .dataSource(dataSource)
                        .structureTable("vendordbs.drugbank_feb_2014")
                        .propertyTable("vendordbs.jchemproperties")
                        .searchOptions("t:d")
                        .outputMode(JChemDBSearcher.OutputMode.MOLECULES)
                );

        from("direct:chemsearch/emolecules_sc")
                .convertBodyTo(String.class)
                .process(new JChemDBSearcher()
                        .dataSource(dataSource)
                        .structureTable("vendordbs.emolecules_ordersc")
                        .propertyTable("vendordbs.jchemproperties")
                        .searchOptions("t:d")
                        .outputMode(JChemDBSearcher.OutputMode.MOLECULES)
                );

        String base = "../../lacfiledrop/dbsearch/emolecules_sc";
        from("file:" + base + "?antInclude=*.mol&move=in")
                .process(new HeaderPropertySetterProcessor(new File(base + "/headers.properties")))
                .to("direct:chemsearch/emolecules_sc")
                .convertBodyTo(InputStream.class)
                .to("file:" + base + "/out?fileName=${file:name.noext}.sdf");

    }

}
