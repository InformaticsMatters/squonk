package org.squonk.rdkit.services;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.api.MimeTypeResolver;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.options.OptionDescriptor.Mode;
import org.squonk.rdkit.db.ChemcentralSearcher;

import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
@ContextName("rdkitsearch")
public class RdkitSearchRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitSearchRestRouteBuilder.class.getName());
    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.rdkit";

    private static final String ROUTE_STATS = "seda:post_stats";

    protected static final HttpServiceDescriptor[] SEARCH_SERVICE_DESCRIPTOR = new HttpServiceDescriptor[]{

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
                                    .withValues(new String[]{"emolecules_order_bb", "emolecules_order_all", "chembl_21"})
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
                                    .withValues(new String[]{"emolecules_order_bb", "emolecules_order_all", "chembl_21"})
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
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                    new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
                    new OptionDescriptor[]{
                            new OptionDescriptor<>(String.class, "query.table", "Table to search", "Structure table to search", Mode.User)
                                    .withValues(new String[]{"emolecules_order_bb", "emolecules_order_all", "chembl_21"})
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
                    StepDefinitionConstants.MoleculeServiceBasicExecutor.CLASSNAME,
                    "multisearch"
            )
    };


    ChemcentralSearcher searcher = new ChemcentralSearcher(ROUTE_STATS);

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

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
                .get().description("ServiceDescriptors for ChemAxon calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(SEARCH_SERVICE_DESCRIPTOR);
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

}
