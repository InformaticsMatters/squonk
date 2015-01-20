package com.im.lac.camel.chemaxon.processor.db;

import chemaxon.jchem.db.JChemSearch;
import chemaxon.sss.SearchConstants;
import chemaxon.sss.search.JChemSearchOptions;
import com.im.lac.chemaxon.db.ConnectionHandlerSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Service;

/**
 * Created by timbo on 26/04/2014.
 */
public abstract class AbstractJChemDBSearcher extends ConnectionHandlerSupport implements Service, Processor {

    private static final Logger LOG = Logger.getLogger(AbstractJChemDBSearcher.class.getName());

    protected String structureTable;
    protected String searchOptions;

    public AbstractJChemDBSearcher() {
    }

    public AbstractJChemDBSearcher(String structureTable, String opts) {
        this.structureTable = structureTable;
        this.searchOptions = opts;
    }

    public void setStructureTable(String structureTable) {
        this.structureTable = structureTable;
    }

    public void setSearchOptions(String searchOptions) {
        this.searchOptions = searchOptions;
    }

    private JChemSearch createJChemSearch() throws SQLException {
        LOG.log(Level.FINE, "Creating JChemSearch for table %s", structureTable);
        JChemSearch j = new JChemSearch();
        j.setStructureTable(structureTable);
        j.setConnectionHandler(getConnectionHandler());
        configureJChemSearch(j);
        return j;
    }

    protected void configureJChemSearch(JChemSearch jcs) {
        // noop
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        JChemSearch jcs = createJChemSearch();

        LOG.fine("Processing search");
        // TODO - evaluate whetehr creating new jcs each time is optimal
        LOG.finer("Initiating search");
        handleSearchParams(exchange, jcs);
        String opts = jcs.getSearchOptions().toString();
        LOG.log(Level.FINER, "Executing search using options: {0}", opts);
        handleQueryStructure(exchange, jcs);
        LOG.finer("Starting search");
        startSearch(exchange, jcs);
        LOG.finer("Search started");
        handleSearchResults(exchange, jcs);
        LOG.fine("Search complete and results sent");
    }

    protected void startSearch(Exchange exchange, JChemSearch jcs) throws Exception {
        jcs.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE);
        jcs.setRunning(true);
    }

    protected void handleQueryStructure(Exchange exchange, JChemSearch jcs) {
        Object body = exchange.getIn().getBody();
        if (body == null) {
            throw new IllegalArgumentException("Query structure must be specified as body");
        }
        if (body instanceof chemaxon.struc.Molecule) {
            jcs.setQueryStructure((chemaxon.struc.Molecule) body);
        } else {
            String query;
            if (body instanceof String) {
                query = (String) body;
            } else {
                query = exchange.getContext().getTypeConverter().convertTo(String.class, body);
            }
            jcs.setQueryStructure(query);
        }
    }

    protected void handleSearchParams(Exchange exchange, JChemSearch jcs) {
        JChemSearchOptions opts = new JChemSearchOptions(SearchConstants.SUBSTRUCTURE);
        if (searchOptions != null) {
            LOG.log(Level.INFO, "Setting default search options to {0}", searchOptions);
            opts.setOptions(searchOptions);
        }
        jcs.setSearchOptions(opts);
    }

    protected abstract void handleSearchResults(Exchange exchange, JChemSearch jcs) throws Exception;
}
