package org.squonk.rdkit.db;

import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.postgresql.ds.PGPoolingDataSource;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.http.CamelRequestResponseExecutor;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.types.Structure;
import org.squonk.rdkit.db.dsl.Select;
import org.squonk.rdkit.db.dsl.WhereClause;
import org.squonk.types.DatasetHandler;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CamelRouteStatsRecorder;
import org.squonk.util.IOUtils;
import org.squonk.util.StatsRecorder;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by timbo on 24/04/16.
 */
public class ChemcentralSearcher {

    private static final Logger LOG = Logger.getLogger(ChemcentralSearcher.class.getName());



    private final DataSource chemchentralDataSource;
    private final String statsRouteUri;

    public ChemcentralSearcher() {
        this(null);
    }

    public ChemcentralSearcher(String statsRouteUri) {
        this.statsRouteUri =statsRouteUri;
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

        String table = exch.getIn().getHeader("table", String.class);
        String mode = exch.getIn().getHeader("mode", String.class);
        Integer limit = exch.getIn().getHeader("limit", Integer.class);
        Boolean chiral = exch.getIn().getHeader("chiral", Boolean.class);
        String fp = exch.getIn().getHeader("fp", String.class);
        String metric = exch.getIn().getHeader("metric", String.class);
        Double threshold = exch.getIn().getHeader("threshold", Double.class);
        LOG.info("Search: table=" + table + " mode=" + mode);

        String query = null;
        MolSourceType molType = null;
        MoleculeObject mo = CamelUtils.readMoleculeObjectFromBody(exch);
        if (mo != null) {
            query = mo.getSource();
            molType = MolSourceType.valueOf(mo.getFormat("smiles").toUpperCase());
        } else {
            // either typedescriptor will do the job
            Structure structure = MoleculeTypeDescriptor.QUERY.readOptionValue(exch.getIn().getHeaders(), "structure");;
            if (structure != null) {
                molType = MolSourceType.valueOf(structure.getFormat().toUpperCase());
                query = structure.getSource();
            }
        }
        if (query == null) {
            throw new IllegalArgumentException("Must provide query structure either as body or as header or query param named 'structure'");
        }

        RDKitTables searcher = new RDKitTables(chemchentralDataSource);
        RDKitTable rdkitTable = searcher.getTable(table);
        if (rdkitTable == null) {
            throw new IllegalArgumentException("Unknown table: " + table);
        }
        List<MoleculeObject> mols = executeSearch(searcher, rdkitTable, table, query, molType, mode, limit, chiral, fp, metric, threshold);
        InputStream json = JsonHandler.getInstance().marshalStreamToJsonArray(mols.stream(), false);
        exch.getOut().setBody(json);
        sendStats(exch, "RDKCart_" + mode, mols.size());
    }


    public void executeMultiSearch(Exchange exch) throws IOException {

        DatasetHandler dh = new DatasetHandler(MoleculeObject.class);
        CamelRequestResponseExecutor executor = new CamelRequestResponseExecutor(exch);
        Dataset<MoleculeObject> dataset = dh.readResponse(executor, true);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }

        String table = exch.getIn().getHeader("table", String.class);
        Integer limit = exch.getIn().getHeader("limit", Integer.class);
        Boolean chiral = exch.getIn().getHeader("chiral", Boolean.class);
        String fp = exch.getIn().getHeader("fp", String.class);
        String metric = exch.getIn().getHeader("metric", String.class);
        Double threshold = exch.getIn().getHeader("threshold", Double.class);

        LOG.info("MultiSearch: table=" + table);

        RDKitTables searcher = new RDKitTables(chemchentralDataSource);
        RDKitTable rdkitTable = searcher.getTable(table);
        if (rdkitTable == null) {
            throw new IllegalArgumentException("Unknown table: " + table);
        }

        Set ids = new ConcurrentSkipListSet<>();
        AtomicInteger count = new AtomicInteger(0);
        Stream<MoleculeObject> results = dataset.getStream().flatMap((mo) -> {
            String query = mo.getSource();
            MolSourceType molType;
            try {
                molType = MolSourceType.valueOf(mo.getFormat() == null ? "SMILES" : mo.getFormat().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid value for MolSourceType enum: " + mo.getFormat(), e);
            }
            List<MoleculeObject> mols = executeSearch(searcher, rdkitTable, table, query, molType, "sim", limit, chiral, fp, metric, threshold);
            count.addAndGet(mols.size());
            LOG.info("Found " + mols.size() + " hits");
            return mols.stream();
        }).filter((mo) -> {
            int id = mo.getValue("id", Integer.class);
            if (ids.contains(id)) {
                LOG.finer("Rejecting " + id);
                return false;
            } else {
                LOG.finer("Accepting " + id);
                ids.add(id);
                return true;
            }
        }).peek((mo) -> {
            mo.getValues().remove("sim");
        }).onClose(() -> {
            sendStats(exch, "RDKCart_multisearch", count.get());
        });
        MoleculeObjectDataset modataset = new MoleculeObjectDataset(results);
        dh.writeResponse(modataset.getDataset(), executor, true);
    }

    private void sendStats(Exchange exch, String key, int count) {
        sendStats(exch, Collections.singletonMap(key, count));
    }

    private void sendStats(Exchange exch, Map<String,Integer> stats) {
        String jobId = exch.getIn().getHeader(StatsRecorder.HEADER_SQUONK_JOB_ID, String.class);
        if (statsRouteUri != null) {
            ProducerTemplate pt = exch.getContext().createProducerTemplate();
            pt.setDefaultEndpointUri(statsRouteUri);
            pt.sendBodyAndHeader(stats, StatsRecorder.HEADER_SQUONK_JOB_ID, jobId);
        }
    }

    private  List<MoleculeObject> executeSearch(
            RDKitTables searcher, RDKitTable rdkitTable,
            String table, String query, MolSourceType molType, String mode, Integer limit,
            Boolean chiral, String fp, String metric, Double threshold) {

        Select select = searcher.createSelectAll(rdkitTable.getName())
                .setChiral((chiral == null || "sim".equals(mode)) ? false : chiral)
                .limit(limit == null ? 1000 : Math.min(1000, limit)).select();

        WhereClause where = select.where();
        switch (mode) {

            case "exact":
                where.exactStructureQuery(query, molType);
                break;

            case "sss":
                where.substructureQuery(query, molType);
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

                where.similarityStructureQuery(query, molType, fpEnum, metricEnum, "sim");
                if (threshold != null) {
                    where.select().setSimilarityThreshold(threshold, metricEnum);
                }

                break;

            default:
                throw new IllegalStateException("Unsupported search mode: " + mode);
        }

        return searcher.executeSelect(where.select());
    }
}
