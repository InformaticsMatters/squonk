package com.im.squonk.rdkit.services;

import com.squonk.rdkit.db.RDkitTableSearch;
import com.im.lac.camel.CamelCommonConstants;
import org.apache.camel.builder.RouteBuilder;
import javax.sql.DataSource;
import org.apache.camel.Exchange;

/**
 * Basic services based on RDKit
 *
 * @author timbo
 */
public class RdkitSearchRouteBuilder extends RouteBuilder {

    public static final String RDKIT_SEARCH_EMOLS_BB = "direct:rdkitEmoleculesBBSearch";

    private final DataSource dataSource;
    //private final RDkitTableSearch searcher;

    public RdkitSearchRouteBuilder(DataSource dataSource) {
        this.dataSource = dataSource;
        //this.searcher = new RDkitTableSearch(dataSource, String schema, String baseTable, MolSourceType molSourceType, Map<String,String> extraColumnDefs);
    }

    @Override
    public void configure() throws Exception {

        from(RDKIT_SEARCH_EMOLS_BB)
                .log("RDKIT_SEARCH_EMOLS_BB starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process((Exchange exch) -> {
                    
                })
                .log("RDKIT_SEARCH_EMOLS_BB finished");

    }
}
