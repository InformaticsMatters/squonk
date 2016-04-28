package org.squonk.rdkit.services;

import com.im.lac.dataset.Metadata;
import com.im.lac.job.jobdef.AsyncHttpProcessDatasetJobDefinition;
import com.im.lac.types.MoleculeObject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.api.MimeTypeResolver;
import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.options.MoleculeTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.rdkit.db.ChemcentralSearcher;

import java.util.logging.Logger;

/**
 * @author timbo
 */
@ContextName("rdkitsearch")
public class RdkitSearchRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(RdkitSearchRestRouteBuilder.class.getName());

    protected static final ServiceDescriptor[] SEARCH_SERVICE_DESCRIPTOR = new ServiceDescriptor[] {

            new ServiceDescriptor(
                    "rdkit.chemcentral.search.structure",
                    "ChemCentral structure search",
                    "Structure search in the ChemCentral database using RDKit PostgreSQL cartridge",
                    new String[]{"search", "rdkit"},
                    null,
                    new String[]{"/Chemistry/Search"},
                    "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                    null,
                    new String[]{"public"},
                    String.class, // inputClass - smiles or smarts
                    MoleculeObject.class, // outputClass
                    Metadata.Type.OPTION, // inputType - taken from the structure option
                    Metadata.Type.STREAM, // outputType
                    "icons/structure_search.png",
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "search",
                                    true, // a relative URL
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null,
                                    null,
                                    null,
                                    null,
                                    new OptionDescriptor[]{

                                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.QUERY,
                                                    new String[] {"smarts"}), "body", "Query Structure", "Structure to use as the query as smiles or smarts")
                                                    .withMinValues(1),

                                            new OptionDescriptor<>(String.class, "query.table", "Table to search", "Structure table to search")
                                                    .withValues(new String[] {"emolecules_order_bb", "emolecules_order_all", "chembl_21"})
                                                    .withMinValues(1),

                                            new OptionDescriptor<>(String.class, "query.mode", "Search mode", "Type of structure to run (exact, substructure, similarity")
                                                    .withValues(new String[] {"exact", "sss"})
                                                    .withMinValues(1),

                                            new OptionDescriptor<>(Integer.class, "query.limit", "Limit", "Max number of hits to return")
                                                    .withDefaultValue(100)

                                     },
                                    StepDefinitionConstants.OutOnlyMoleculeServiceExecutor.CLASSNAME)
                    }
            ),


            new ServiceDescriptor(
                    "rdkit.chemcentral.search.similarity",
                    "ChemCentral similarity search",
                    "Similarity search in the ChemCentral database using RDKit PostgreSQL cartridge",
                    new String[]{"search", "rdkit"},
                    null,
                    new String[]{"/Chemistry/Search"},
                    "Tim Dudgeon <tdudgeon@informaticsmatters.com>",
                    null,
                    new String[]{"public"},
                    String.class, // inputClass - smiles or smarts
                    MoleculeObject.class, // outputClass
                    Metadata.Type.OPTION, // inputType - taken from the structure option
                    Metadata.Type.STREAM, // outputType
                    "icons/structure_search.png",
                    new AccessMode[]{
                            new AccessMode(
                                    "asyncHttp",
                                    "Immediate execution",
                                    "Execute as an asynchronous REST web service",
                                    "search",
                                    true, // a relative URL
                                    AsyncHttpProcessDatasetJobDefinition.class,
                                    null,
                                    null,
                                    null,
                                    null,
                                    new OptionDescriptor[]{

                                            new OptionDescriptor<>(new MoleculeTypeDescriptor(MoleculeTypeDescriptor.MoleculeType.QUERY,
                                                    new String[] {"smiles"}), "body", "Query Structure", "Structure to use as the query as smiles or smarts")
                                                    .withMinValues(1),

                                            new OptionDescriptor<>(String.class, "query.table", "Table to search", "Structure table to search")
                                                    .withValues(new String[] {"emolecules_order_bb", "emolecules_order_all", "chembl_21"})
                                                    .withMinValues(1),

                                            new OptionDescriptor<>(String.class, "query.mode", "Search mode", "Type of structure to run (exact, substructure, similarity")
                                                    .withDefaultValue("sim")
                                                    //.withAccess(true, false) // change this to false, false once the visitbility bug is fixed
                                                    .withMinValues(1),

                                            new OptionDescriptor<>(Float.class, "query.threshold", "Similarity Cuttoff", "Similarity score cuttoff between 0 and 1 (1 means identical)").withDefaultValue(0.7f),

                                            new OptionDescriptor<>(String.class, "query.fp", "Fingerprint type", "Type of fingerprint to use for similarity search")
                                                    .withValues(new String[] {"RDKIT", "MORGAN_CONNECTIVITY_2", "MORGAN_FEATURE_2"}),

                                            new OptionDescriptor<>(String.class, "query.metric", "Similarity Metric", "Type of metric to use for similarity distance")
                                                    .withValues(new String[] {"TANIMOTO", "DICE"})  ,

                                            new OptionDescriptor<>(Integer.class, "query.limit", "Limit", "Max number of hits to return")
                                                    .withDefaultValue(100)

                                    },
                                    StepDefinitionConstants.OutOnlyMoleculeServiceExecutor.CLASSNAME)
                    }
            )
    };


    ChemcentralSearcher searcher = new ChemcentralSearcher();

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0");

        /* These are the REST endpoints - exposed as public web services 
         */

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
                .endRest();
    }

}
