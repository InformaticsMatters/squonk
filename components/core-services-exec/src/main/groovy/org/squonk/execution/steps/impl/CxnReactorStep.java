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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.StatsRecorder;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.squonk.execution.steps.StepDefinitionConstants.CxnReactor.*;

/**
 * @author timbo
 */
public class CxnReactorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(CxnReactorStep.class.getName());


    public static final String VAR_OUTPUT = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    private String endpoint = "http://chemservices:8080/chem-services-chemaxon-basic/rest/v1/reactor/react";


    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs, CamelContext context) throws Exception {
        String reactionName = getOption(OPTION_REACTION, String.class);
        if (reactionName == null) {
            throw new IllegalStateException("Reaction must be specified");
        }

        boolean ignoreReactivity = getOption(OPTION_IGNORE_REACTIVITY, Boolean.class, false);
        boolean ignoreSelectivity = getOption(OPTION_IGNORE_SELECTIVITY, Boolean.class, false);
        boolean ignoreTolerance = getOption(OPTION_IGNORE_TOLERANCE, Boolean.class, false);

        String outputFormat = getOption(OPTION_OUTPUT_FORMAT, String.class, "smiles");

        Dataset<MoleculeObject> reactants1 = (Dataset<MoleculeObject>)inputs.get(VARIABLE_R1);
        Dataset<MoleculeObject> reactants2 = (Dataset<MoleculeObject>)inputs.get(VARIABLE_R2);

        if (reactants1 == null || reactants2 == null) {
            // TODO - allow single reactants
            throw new IllegalStateException("Must specify reactants for Reactant1 and Reactant2");
        }

        statusMessage = "Setting reactant IDs ...";
        final AtomicInteger c1 = new AtomicInteger(0);
        Stream r1stream = reactants1.getStream().peek((mo) -> {
            mo.putValue("R1_REACTANT", "R1_" + c1.incrementAndGet());
        });
        final AtomicInteger c2 = new AtomicInteger(0);
        Stream r2stream = reactants2.getStream().peek((mo) -> {
            mo.putValue("R2_REACTANT", "R2_" + c2.incrementAndGet());
        });

        Stream<MoleculeObject> reactants = Stream.concat(r1stream, r2stream);
        InputStream input = JsonHandler.getInstance().marshalStreamToJsonArray(reactants, false);
        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        requestHeaders.put(StepDefinitionConstants.CxnReactor.OPTION_REACTION, reactionName);
        requestHeaders.put(StepDefinitionConstants.CxnReactor.OPTION_IGNORE_REACTIVITY, ignoreReactivity);
        requestHeaders.put(StepDefinitionConstants.CxnReactor.OPTION_IGNORE_SELECTIVITY, ignoreSelectivity);
        requestHeaders.put(StepDefinitionConstants.CxnReactor.OPTION_IGNORE_TOLERANCE, ignoreTolerance);
        requestHeaders.put(StepDefinitionConstants.CxnReactor.OPTION_OUTPUT_FORMAT, outputFormat);
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");
        if (jobId != null) {
            requestHeaders.put(StatsRecorder.HEADER_SQUONK_JOB_ID, jobId);
        }


        // send for execution
        statusMessage = "Posting request ...";
        Map<String, Object> responseHeaders = new HashMap<>();
        InputStream results = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST", endpoint, input, requestHeaders, responseHeaders, options);
        statusMessage = "Handling results ...";

        // start debug output
        //String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(results), 1000);
        //LOG.info("Results: |" + data + "|");
        //results = new ByteArrayInputStream(data.getBytes());
        // end debug output

        Dataset<MoleculeObject> output = new Dataset<>(results, new DatasetMetadata(MoleculeObject.class));
        Stream<MoleculeObject> stream = output.getStream();
        Stream output2 = addStreamCounter(stream, "Enumerated %s products");
        output.replaceStream(output2);
        return Collections.singletonMap(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, output);
    }


}
