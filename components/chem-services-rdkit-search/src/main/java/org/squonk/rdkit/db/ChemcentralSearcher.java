/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.rdkit.db;

import org.squonk.config.SquonkServerConfig;
import org.squonk.http.RequestInfo;
import org.squonk.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
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
import org.squonk.util.*;

import static org.squonk.util.Metrics.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 24/04/16.
 */
public class ChemcentralSearcher {

    private static final Logger LOG = Logger.getLogger(ChemcentralSearcher.class.getName());

    private static final String CODE_SIM = Metrics.generate(PROVIDER_RDKIT, METRICS_STRUCTURE_SEARCH_SIMILARITY);
    private static final String CODE_SSS = Metrics.generate(PROVIDER_RDKIT, METRICS_STRUCTURE_SEARCH_SSS);
    private static final String CODE_EXACT = Metrics.generate(PROVIDER_RDKIT, METRICS_STRUCTURE_SEARCH_EXACT);

    private final DataSource chemchentralDataSource;
    private final String statsRouteUri;

    public ChemcentralSearcher() {
        this(null);
    }

    public ChemcentralSearcher(String statsRouteUri) {
        this.statsRouteUri = statsRouteUri;
        String pw = IOUtils.getConfiguration("POSTGRES_SQUONK_PASSWORD", "squonk");
        this.chemchentralDataSource = SquonkServerConfig.createDataSource("postgres", new Integer(5432), "squonk", pw, "chemcentral");
    }

    public void executeSearch(Exchange exch) throws IOException {

        // get all the info about the requested input and output
        RequestInfo requestInfo = RequestInfo.build(
                new String[] {CommonMimeTypes.MIME_TYPE_MDL_MOLFILE},
                new String[] {CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON},
                exch);

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

        if (requestInfo.isGzipAccept()) {
            exch.getOut().setHeader("Content-Encoding", "gzip");
        }
        // setting the gzip header handles the compression
        InputStream json = JsonHandler.getInstance().marshalStreamToJsonArray(mols.stream(), false);
        exch.getOut().setBody(json);

        String code = null;
        switch (mode) {
            case "exact":
                code = CODE_EXACT;
                break;
            case "sss":
                code = CODE_SSS;
                break;
            case "sim":
                code = CODE_SIM;
                break;
        }
        if (code != null) {
            sendStats(exch, code, mols.size(), table);
        }
    }


    public void executeMultiSearch(Exchange exch) throws IOException {

        RequestInfo requestInfo = RequestInfo.build(
                new String[] {CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON},
                new String[] {CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON},
                exch);

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
            sendStats(exch, CODE_SIM, count.get(), table);
        });
        MoleculeObjectDataset modataset = new MoleculeObjectDataset(results);
        dh.writeResponse(modataset.getDataset(), executor, true);
    }

    private void sendStats(Exchange exch, String key, int count, String table) {

        String jobId = exch.getIn().getHeader(StatsRecorder.HEADER_SQUONK_JOB_ID, String.class);
        if (statsRouteUri != null) {
            if (jobId == null) {
                LOG.info("No job ID defined - can't post usage stats");
                return;
            }
            ProducerTemplate pt = exch.getContext().createProducerTemplate();
            pt.setDefaultEndpointUri(statsRouteUri);
            Map<String,Integer> stats = new HashMap<>();
            stats.put(key, count);
            stats.put(PROVIDER_DATA_TABLE + "." + table, count);
            ExecutionStats es = new ExecutionStats(jobId, stats);
            pt.sendBody(es);
        } else {
            LOG.info("No stats route defined - can't post usage stats");
        }
    }

    private  List<MoleculeObject> executeSearch(
            RDKitTables searcher, RDKitTable rdkitTable,
            String table, String query, MolSourceType molType, String mode, Integer limit,
            Boolean chiral, String fp, String metric, Double threshold) {

        Select select = searcher.createSelectAll(rdkitTable.getName())
                .setChiral((chiral == null || "sim".equals(mode)) ? false : chiral)
                .limit(limit == null ? 100 : Math.min(10000, limit)).select();

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
