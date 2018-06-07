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

package org.squonk.rdkit.services;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.management.event.CamelContextStartedEvent;
import org.apache.camel.management.event.RouteStartedEvent;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.squonk.api.MimeTypeResolver;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.rdkit.db.ChemcentralSearcher;
import org.squonk.rdkit.db.ChemcentralConfig;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
public class RdkitSearchRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitSearchRestRouteBuilder.class.getName());
    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.rdkit";

    private static final String ROUTE_STATS = "seda:post_stats";
    private static final String ROUTE_POST_SDS = "direct:post-service-descriptors";

    private ChemcentralConfig config = new ChemcentralConfig(ROUTE_STATS);
    private ChemcentralSearcher searcher = new ChemcentralSearcher(config);
    private String[] shortTableNames = config.getShortTableNames();

    protected final HttpServiceDescriptor[] SEARCH_SERVICE_DESCRIPTORS = new HttpServiceDescriptor[]{

            new HttpServiceDescriptor(
                    "rdkit.chemcentral.search.structure",
                    "ChemCentral structure search",
                    "Structure search in the ChemCentral database using RDKit PostgreSQL cartridge",
                    new String[]{"search", "rdkit"},
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Chemcentral+Structure+Search",
                    "icons/structure_search.png",
                    null, // inputType - taken from the structure option
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(MoleculeTypeDescriptor.QUERY,
                                    "body", "Query Structure", "Structure to use as the query as mol, smarts or smiles", Mode.User)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.table", "Table to search", "Structure table to search", Mode.User)
                                    .withValues(shortTableNames)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.mode", "Search mode", "Type of structure to run (exact, substructure, similarity", Mode.User)
                                    .withValues(new String[]{"exact", "sss"})
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(Integer.class, "query.limit", "Limit", "Max number of hits to return", Mode.User)
                                    .withDefaultValue(100)
                                    .withMinMaxValues(1, 1)
                    },
                    StepDefinitionConstants.OutOnlyMoleculeServiceExecutor.CLASSNAME,
                    "search"

            ),
            new HttpServiceDescriptor(
                    "rdkit.chemcentral.search.similarity",
                    "ChemCentral similarity search",
                    "Similarity search in the ChemCentral database using RDKit PostgreSQL cartridge",
                    new String[]{"search", "rdkit"},
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Chemcentral+Similarity+Search",
                    "icons/structure_search.png",
                    null, // inputType - taken from the structure option
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(MoleculeTypeDescriptor.DISCRETE,
                                    "body", "Query Structure", "Structure to use as the query as smiles or smarts", Mode.User)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.table", "Table to search", "Structure table to search", Mode.User)
                                    .withValues(shortTableNames)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.mode", "Search mode", "Type of structure to run (exact, substructure, similarity)", Mode.User)
                                    .withDefaultValue("sim")
                                    .withAccess(false, false) // needs to be invisible
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(Float.class, "query.threshold", "Similarity Cuttoff", "Similarity score cuttoff between 0 and 1 (1 means identical)", Mode.User)
                                    .withDefaultValue(0.7f)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.fp", "Fingerprint type", "Type of fingerprint to use for similarity search", Mode.User)
                                    .withValues(new String[]{"RDKIT", "MORGAN_CONNECTIVITY_2", "MORGAN_FEATURE_2"})
                                    .withDefaultValue("RDKIT")
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.metric", "Similarity Metric", "Type of metric to use for similarity distance", Mode.User)
                                    .withValues(new String[]{"TANIMOTO", "DICE"})
                                    .withDefaultValue("TANIMOTO")
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(Integer.class, "query.limit", "Limit", "Max number of hits to return", Mode.User)
                                    .withDefaultValue(100)
                                    .withMinMaxValues(1, 1)
                    },
                    StepDefinitionConstants.OutOnlyMoleculeServiceExecutor.CLASSNAME,
                    "search"

            ),
            new HttpServiceDescriptor(
                    "rdkit.chemcentral.multisearch",
                    "ChemCentral multi search",
                    "Similarity search for multiple queries in the ChemCentral database using RDKit PostgreSQL cartridge",
                    new String[]{
                            "search", "rdkit"
                    },
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/Chemcentral+multi-search",
                    "icons/structure_search.png",
                    IODescriptors.createMoleculeObjectDataset("input"),
                    IODescriptors.createMoleculeObjectDataset("output"),
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(String.class, "query.table", "Table to search", "Structure table to search", Mode.User)
                                    .withValues(shortTableNames)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(Float.class, "query.threshold", "Similarity Cuttoff", "Similarity score cuttoff between 0 and 1 (1 means identical)", Mode.User)
                                    .withDefaultValue(0.7f)
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.fp", "Fingerprint type", "Type of fingerprint to use for similarity search", Mode.User)
                                    .withValues(new String[]{"RDKIT", "MORGAN_CONNECTIVITY_2", "MORGAN_FEATURE_2"})
                                    .withDefaultValue("RDKIT")
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(String.class, "query.metric", "Similarity Metric", "Type of metric to use for similarity distance", Mode.User)
                                    .withValues(new String[]{"TANIMOTO", "DICE"})
                                    .withDefaultValue("TANIMOTO")
                                    .withMinMaxValues(1, 1),
                            new OptionDescriptor<>(Integer.class, "query.limit", "Limit", "Max number of hits to return", Mode.User)
                                    .withDefaultValue(100)
                                    .withMinMaxValues(1, 1),
                    },
                    new ThinDescriptor("input", null),
                    StepDefinitionConstants.MoleculeServiceBasicExecutor.CLASSNAME,
                    "multisearch"
            )
    };

    protected ServiceDescriptorSet sdset = new ServiceDescriptorSet(
            "http://chemcentral-search:8080/chemcentral-search/rest/v1/db",
            "http://chemcentral-search:8080/chemcentral-search/rest/ping",
            Arrays.asList(SEARCH_SERVICE_DESCRIPTORS));

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        from(ROUTE_POST_SDS)
                .log(ROUTE_POST_SDS)
                .process((Exchange exch) -> {
                    String json = JsonHandler.getInstance().objectToJson(sdset);
                    exch.getOut().setBody(json);
                    exch.getOut().setHeader(Exchange.CONTENT_TYPE, CommonMimeTypes.MIME_TYPE_JSON);
                })
                .to("http4:coreservices:8080/coreservices/rest/v1/services");

        // send usage metrics to the message queue
        from(ROUTE_STATS)
                .log("Posting stats: ${body}")
                .marshal().json(JsonLibrary.Jackson)
                .to(mqueueUrl);

        // These are the REST endpoints - exposed as public web services

        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();

        rest("/v1/db").description("ChemCentral search using RDKit")
                // service descriptor
                .get().description("ServiceDescriptors for Chemcentral search")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(SEARCH_SERVICE_DESCRIPTORS);
                })
                .endRest()
                .get("search")
                .produces(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)
                .bindingMode(RestBindingMode.off)
                .route()
                .process((Exchange exch) -> {
                    searcher.executeSearch(exch);
                })
                .endRest()
                .post("search")
                .consumes(MimeTypeResolver.MIME_TYPE_DAYLIGHT_SMILES)
                .produces(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)
                .bindingMode(RestBindingMode.off)
                .route()
                .process((Exchange exch) -> {
                    searcher.executeSearch(exch);
                })
                .endRest()
                .post("multisearch")
                .consumes(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)
                .produces(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)
                .bindingMode(RestBindingMode.off)
                .route()
                .process((Exchange exch) -> {
                    searcher.executeMultiSearch(exch);
                })
                .endRest();
    }

    void onContextStarted(@Observes @Default CamelContextStartedEvent event) {
        LOG.fine("Context started");

        LOG.info("Posting service descriptors");
        try {
            ProducerTemplate pt = event.getContext().createProducerTemplate();
            String result = pt.requestBody(ROUTE_POST_SDS, "", String.class);
            LOG.info("Response was: " + result);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to post service descriptors", e);
        }
    }

}
