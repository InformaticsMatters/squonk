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

package org.squonk.smartcyp.services;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.management.event.CamelContextStartedEvent;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.processor.MoleculeObjectRouteHttpProcessor;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.OptionDescriptor;
import org.squonk.smartcyp.SMARTCypRunner;
import org.squonk.types.TypeResolver;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_BASIC_JSON;
import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * @author timbo
 */
public class SMARTCypRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(SMARTCypRestRouteBuilder.class.getName());

    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.smartcyp";

    @Inject
    private TypeResolver resolver;

    private static final String ROUTE_SMARTCYP = "smartcyp";
    private static final String ROUTE_STATS = "seda:post_stats";
    private static final String ROUTE_POST_SDS = "direct:post-service-descriptors";


    protected static final HttpServiceDescriptor[] CALCULATORS_SERVICE_DESCRIPTOR
            = new HttpServiceDescriptor[]{
            createServiceDescriptor(
                    "smartcyp.predict", "SMARTCyp", "Cytochrome P450 metabolism prediction using SMARTCyp",
                    new String[]{"p450", "cytochrome", "metabolism", "molecularproperties", "smartcyp"},
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/SMARTCyp",
                    "icons/properties_add.png", ROUTE_SMARTCYP, createSMARTCypOptionDescriptors())
    };

    protected ServiceDescriptorSet sdset = new ServiceDescriptorSet(
            "http://chemcentral-search:8080/chemcentral-search/rest/v1/db",
            "http://chemcentral-search:8080/chemcentral-search/rest/ping",
            Arrays.asList(CALCULATORS_SERVICE_DESCRIPTOR));


    private static HttpServiceDescriptor createServiceDescriptor(String id, String name, String description, String[] tags, String resourceUrl, String icon, String endpoint, OptionDescriptor[] options) {

        return new HttpServiceDescriptor(
                id,
                name,
                description,
                tags,
                resourceUrl,
                icon,
                new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("input")},
                new IODescriptor[] {IODescriptors.createMoleculeObjectDataset("output")},
                options,
                StepDefinitionConstants.MoleculeServiceThinExecutor.CLASSNAME,
                endpoint
        );
    }

    static private OptionDescriptor[] createSMARTCypOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(Boolean.class, "query." + SMARTCypRunner.PARAM_GEN, "Calculate general", "Perform the general P450 calculation", OptionDescriptor.Mode.User)
                .withDefaultValue(true)
                .withMinMaxValues(1,1));
        list.add(new OptionDescriptor<>(Boolean.class, "query." + SMARTCypRunner.PARAM_2D6, "Calculate 2D6", "Perform the Cyp 2D6 calculation", OptionDescriptor.Mode.User)
                .withDefaultValue(true)
                .withMinMaxValues(1,1));
        list.add(new OptionDescriptor<>(Boolean.class, "query." + SMARTCypRunner.PARAM_2C9, "Calculate 2C9", "Perform the Cyp 2C9 calculation", OptionDescriptor.Mode.User)
                .withDefaultValue(true)
                .withMinMaxValues(1,1));
        list.add(new OptionDescriptor<>(Integer.class, "query." +  SMARTCypRunner.PARAM_MAX_RANK,
                "Max rank", "Rank above which results are not included", OptionDescriptor.Mode.User)
                .withMinMaxValues(0,1)
                .withDefaultValue(3));
        list.add(new OptionDescriptor<>(Float.class, "query." + SMARTCypRunner.PARAM_SCORE,
                "Score threshold", "Threshold above which results are not included", OptionDescriptor.Mode.User)
                .withMinMaxValues(0,1)
                .withDefaultValue(100f));
        list.add(new OptionDescriptor<>(Boolean.class, "query." + SMARTCypRunner.PARAM_NOXID_CORRECTION,
                "Empirical N-Oxidation Corrections", "Apply Empirical Nitrogen Oxidation Corrections", OptionDescriptor.Mode.User)
                .withDefaultValue(true)
                .withMinMaxValues(1,1));

        return list.toArray(new OptionDescriptor[0]);
    }


    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "SMARTCyp services").apiProperty("api.version", "1.0")
                .apiProperty("cors", "true");

        from(ROUTE_POST_SDS)
                .log(ROUTE_POST_SDS)
                .process((Exchange exch) -> {
                    String json = JsonHandler.getInstance().objectToJson(sdset);
                    exch.getOut().setBody(json);
                    exch.getOut().setHeader(Exchange.CONTENT_TYPE, CommonMimeTypes.MIME_TYPE_SERVICE_DESCRIPTOR_SET + "+json");
                })
                .to("http4:coreservices:8080/coreservices/rest/v1/services");


        // send usage metrics to the message queue
        from(ROUTE_STATS)
                .marshal().json(JsonLibrary.Jackson)
                .to(mqueueUrl);

        //These are the REST endpoints - exposed as public web services
        //
        // test like this:
        // curl "http://localhost:8080/chem-services-smartcyp/rest/ping"
        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();


        // test like this:
        // curl -X POST -T mols.json "http://localhost:8080/chem-services-smartcyp/rest/v1/smartcyp"
        rest("/v1").description("Property calculation services using SMARTCyp")
                .bindingMode(RestBindingMode.off)
                //
                // service descriptor
                .get().description("ServiceDescriptors for SMARTCyp calculators")
                .bindingMode(RestBindingMode.json)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    exch.getIn().setBody(CALCULATORS_SERVICE_DESCRIPTOR);
                })
                .endRest()
                //
                .post(ROUTE_SMARTCYP).description(CALCULATORS_SERVICE_DESCRIPTOR[0].getServiceConfig().getDescription())
                .consumes(join(MoleculeObjectRouteHttpProcessor.DEFAULT_INPUT_MIME_TYPES))
                .produces(join(MIME_TYPE_DATASET_MOLECULE_JSON, MIME_TYPE_DATASET_BASIC_JSON))
                .route()
                .process(new MoleculeObjectRouteHttpProcessor(SMARTCypCalculatorsRouteBuilder.SMARTCyp_predict, resolver, ROUTE_STATS))
                .endRest();

    }

    String join(String... args) {
        return Stream.of(args).collect(Collectors.joining(","));
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
