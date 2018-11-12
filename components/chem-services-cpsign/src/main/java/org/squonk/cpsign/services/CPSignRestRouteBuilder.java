/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.cpsign.services;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.squonk.camel.processor.CPSignTrainProcessor;
import org.squonk.camel.processor.DefaultMoleculeObjectRouteHttpProcessor;
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.NumberRange;
import org.squonk.types.TypeResolver;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;
import static org.squonk.util.CommonMimeTypes.MIME_TYPE_CPSIGN_TRAIN_RESULT;

/**
 * @author timbo
 */
public class CPSignRestRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(CPSignRestRouteBuilder.class.getName());

    private final String mqueueUrl = new MessageQueueCredentials().generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
            "&routingKey=tokens.cpsign";

    @Inject
    private TypeResolver resolver;

    private static final String ROUTE_CPSIGN_TRAIN = "train";
    private static final String ROUTE_CPSIGN_PREDICT = "predict";

    private static final String ROUTE_STATS = "seda:post_stats";


    protected static final HttpServiceDescriptor[] SERVICE_DESCRIPTORS
            = new HttpServiceDescriptor[]{
            createServiceDescriptor(
                    "cpsign.regression.train", "CPSign Regression Train", "Train a regression predictive model using CPSign",
                    new String[]{"predictivemodel", "machinelearning", "cpsign"},
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/CPSign",
                    "icons/properties_add.png", ROUTE_CPSIGN_TRAIN, createRegressionOptionDescriptors()),

            createServiceDescriptor(
                    "cpsign.classifiction.train", "CPSign Classification Train", "Train a classification predictive model using CPSign",
                    new String[]{"predictivemodel", "machinelearning", "cpsign"},
                    "https://squonk.it/xwiki/bin/view/Cell+Directory/Data/CPSign",
                    "icons/properties_add.png", ROUTE_CPSIGN_TRAIN, createClassificationOptionDescriptors())
    };


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
                StepDefinitionConstants.DatasetHttpExecutor.CLASSNAME,
                endpoint
        );
    }

    static private OptionDescriptor[] createRegressionOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(String.class, "query." + CPSignTrainProcessor.HEADER_PREDICT_TYPE,
                "Prediction type", "Conformal prediction type [Regression, Classification]", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1)
                .withDefaultValue("Regression")
                .withAccess(false, false));

        addModelTypeOptions(list);

        list.add(new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[]{Float.class, Double.class, Integer.class}),
                "query." + CPSignTrainProcessor.HEADER_FIELD_NAME, "Field with values",
                "Name of the field containing the values to train with", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1));

        addModelParameterOptions(list);

        return list.toArray(new OptionDescriptor[0]);
    }

    static private OptionDescriptor[] createClassificationOptionDescriptors() {
        List<OptionDescriptor> list = new ArrayList<>();

        list.add(new OptionDescriptor<>(String.class, "query." + CPSignTrainProcessor.HEADER_PREDICT_TYPE,
                "Prediction type", "Conformal prediction type [Regression, Classification]", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1)
                .withDefaultValue("Classification")
                .withAccess(false, false));

        addModelTypeOptions(list);

        list.add(new OptionDescriptor<>(new DatasetFieldTypeDescriptor(new Class[0]),
                "query." + CPSignTrainProcessor.HEADER_FIELD_NAME, "Field with values",
                "Name of the field containing the values to train with", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1));
        addFieldValuesOptions(list);

        addModelParameterOptions(list);

        return list.toArray(new OptionDescriptor[0]);
    }

    private static void addModelTypeOptions(List<OptionDescriptor> list) {
        list.add(new OptionDescriptor<>(String.class, "query." + CPSignTrainProcessor.HEADER_PREDICT_METHOD,
                "Prediction method", "Conformal prediction method [CCP]", OptionDescriptor.Mode.User)
                .withValues(new String[]{"CCP"})
                .withMinMaxValues(1, 1)
                .withDefaultValue("CCP"));
        list.add(new OptionDescriptor<>(String.class, "query." + CPSignTrainProcessor.HEADER_PREDICT_LIBRARY,
                "Prediction library", "Conformal prediction library [LibSVM, LibLinear]", OptionDescriptor.Mode.User)
                .withValues(new String[]{"LibSVM", "LibLinear"})
                .withMinMaxValues(1, 1)
                .withDefaultValue("LibSVM"));
    }

    private static void addFieldValuesOptions(List<OptionDescriptor> list) {
        list.add(new OptionDescriptor<>(String.class, "query." + CPSignTrainProcessor.HEADER_VALUE_1,
                "Class 1 value", "Class 1 value", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1));
        list.add(new OptionDescriptor<>(String.class, "query." + CPSignTrainProcessor.HEADER_VALUE_2,
                "Class 2 value", "Class 2 value", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1));
    }

    private static void addModelParameterOptions(List<OptionDescriptor> list) {
        list.add(new OptionDescriptor<>(Integer.class, "query." + CPSignTrainProcessor.HEADER_CV_FOLDS,
                "Cross validation folds", "Number of cross validation folds", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1)
                .withDefaultValue(5));
        list.add(new OptionDescriptor<>(Float.class, "query." + CPSignTrainProcessor.HEADER_CONFIDENCE,
                "Cross validation confidence level", "Cross validation confidence level", OptionDescriptor.Mode.User)
                .withMinMaxValues(1, 1)
                .withDefaultValue(0.7f));
        list.add(new OptionDescriptor<>(NumberRange.Integer.class, "query." + CPSignTrainProcessor.HEADER_SIGNATURE_HEIGHT,
                "Signature height", "Signature height range", OptionDescriptor.Mode.User)
                .withMinMaxValues(1,1)
                .withDefaultValue(new NumberRange.Integer(1, 3)));
    }


    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").host("0.0.0.0")
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "CPSign services").apiProperty("api.version", "1.0")
                .apiProperty("cors", "true");

        // send usage metrics to the message queue
        from(ROUTE_STATS)
                .marshal().json(JsonLibrary.Jackson)
                .to(mqueueUrl);

        //These are the REST endpoints - exposed as public web services
        //
        // test like this:
        // curl "http://localhost:8080/chem-services-cpsign/rest/ping"
        rest("/ping").description("Simple ping service to check things are running")
                .get()
                .produces("text/plain")
                .route()
                .transform(constant("OK\n")).endRest();


        // test like this:
        // curl -X POST -T mols.json "http://localhost:8080/chem-services-cpsign/rest/v1/train"
        rest("/v1").description("Predictive modeling services using CPSign")
                .bindingMode(RestBindingMode.off)
                //
                // service descriptor
                .get().description("ServiceDescriptors for CPSign predictive modeling tools")
                .bindingMode(RestBindingMode.off)
                .produces("application/json")
                .route()
                .process((Exchange exch) -> {
                    String json = JsonHandler.getInstance().objectToJson(SERVICE_DESCRIPTORS);
                    exch.getIn().setBody(json);
                })
                .endRest()
                //
                .post(ROUTE_CPSIGN_TRAIN).description(SERVICE_DESCRIPTORS[0].getServiceConfig().getDescription())
                .consumes(join(MIME_TYPE_DATASET_MOLECULE_JSON))
                .produces(join(MIME_TYPE_CPSIGN_TRAIN_RESULT))
                .route()
                .process(new DefaultMoleculeObjectRouteHttpProcessor(CPSignPredictRouteBuilder.CPSign_train, resolver,
                        new String[] {CommonMimeTypes.MIME_TYPE_CPSIGN_TRAIN_RESULT}, ROUTE_STATS, false))
                .endRest();
    }

    String join(String... args) {
        return Stream.of(args).collect(Collectors.joining(","));
    }

}
