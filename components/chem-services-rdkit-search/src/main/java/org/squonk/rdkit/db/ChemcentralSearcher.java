package org.squonk.rdkit.db;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.postgresql.ds.PGPoolingDataSource;
import org.squonk.rdkit.db.dsl.Select;
import org.squonk.rdkit.db.dsl.WhereClause;
import org.squonk.rdkit.db.impl.DbSearcher;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by timbo on 24/04/16.
 */
public class ChemcentralSearcher {

    private static final Logger LOG = Logger.getLogger(ChemcentralSearcher.class.getName());

    private DataSource chemchentralDataSource;

    public ChemcentralSearcher() {
        String s = IOUtils.getConfiguration("SQUONK_DB_SERVER", "localhost");
        String po = IOUtils.getConfiguration("SQUONK_DB_PORT", "5432");
        String pw = IOUtils.getConfiguration("POSTGRES_SQUONK_PASS", "squonk");

        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(s);
        dataSource.setPortNumber(new Integer(po));
        dataSource.setDatabaseName("chemcentral");
        dataSource.setUser("squonk");
        dataSource.setPassword(pw);

        LOG.info("Search using server=" + s);

        this.chemchentralDataSource = dataSource;
    }

    public void executeSearch(Exchange exch) throws IOException {

        String query = null;
        if (exch.getIn().getBody() != null) {
            query = exch.getIn().getBody(String.class);
        }
        if (query == null || query.isEmpty()) {
            query = exch.getIn().getHeader("q", String.class);
        }
        if (query == null|| query.isEmpty()) {
            throw new IllegalArgumentException("Must provide query structure either as body or query param named 'q'");
        }
        LOG.info("q=" + query);

        String table = exch.getIn().getHeader("table", String.class);
        String mode = exch.getIn().getHeader("mode", String.class);
        Integer limit = exch.getIn().getHeader("limit", Integer.class);
        Boolean chiral = exch.getIn().getHeader("chiral", Boolean.class);
        String fp = exch.getIn().getHeader("fp", String.class);
        String metric = exch.getIn().getHeader("metric", String.class);
        Double threshold = exch.getIn().getHeader("threshold", Double.class);

        LOG.info("Search: table=" + table + " mode=" + mode + " q=" + query);

        DbSearcher searcher = new DbSearcher(chemchentralDataSource);
        RDKitTable rdkitTable = searcher.getTable(table);
        if (rdkitTable == null) {
            throw new IllegalArgumentException("Unknown table: " + table);
        }

        Select select = searcher.createSelect(rdkitTable.getName())
                .setChiral(chiral == null ? false : chiral)
                .limit(limit == null ? 1000 : Math.min(1000, limit)).select();


        WhereClause where = select.where();
        switch (mode) {

            case "exact":
                where.exactStructureQuery(query);
                break;

            case "sss":
                where.substructureQuery(query);
                break;

            case "sim":
                FingerprintType fpEnum = null;
                if (fp == null) {
                    fpEnum = rdkitTable.getFingerprintTypes().get(0);
                } else {
                    fpEnum = FingerprintType.valueOf(fp.toUpperCase());
                }

                Metric metricEnum = null;
                if (metric == null) {
                    metricEnum = Metric.values()[0];
                } else {
                    metricEnum = Metric.valueOf(metric.toUpperCase());
                }

                where.similarityStructureQuery(query, fpEnum, metricEnum, "sim");
                if (threshold != null) {
                    where.select().setSimilarityThreshold(threshold, metricEnum);
                }

                break;

            default:
                throw new IllegalStateException("Unsupported search mode: " + mode);
        }

        List<MoleculeObject> mols = searcher.executeSelect(where.select());
        InputStream json = JsonHandler.getInstance().marshalStreamToJsonArray(mols.stream(), false);
        exch.getOut().setBody(json);
    }
}
