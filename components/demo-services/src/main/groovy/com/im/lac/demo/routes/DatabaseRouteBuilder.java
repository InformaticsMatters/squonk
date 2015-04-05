package com.im.lac.demo.routes;

import com.im.lac.camel.processor.HeaderPropertySetterProcessor;
import com.im.lac.camel.chemaxon.processor.db.JChemDBSearcher;
import java.io.File;
import java.io.InputStream;
import javax.sql.DataSource;
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
                        .structureTable("vendordbs.emolecules_ordersc")
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
                        .structureTable("vendordbs.emolecules_orderbb")
                        .propertyTable("vendordbs.jchemproperties")
                        .searchOptions("t:d")
                        .searchOptionsOverride("maxResults:5000 maxTime:30000")
                        .outputColumn("version_id")
                        .outputMode(JChemDBSearcher.OutputMode.MOLECULE_OBJECTS)
                        .structureFormat("smiles:-a")
                );

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

}
