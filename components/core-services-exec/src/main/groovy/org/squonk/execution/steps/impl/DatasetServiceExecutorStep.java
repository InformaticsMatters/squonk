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

package org.squonk.execution.steps.impl;

import org.squonk.execution.steps.AbstractStep;
import org.squonk.types.MoleculeObject;
import org.apache.camel.CamelContext;
import org.squonk.camel.util.CamelUtils;
import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.types.io.JsonHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Reads a Dataset and sends it to a service, writing the results without caring what type they are. Typically used when
 * needing to convert to a specific format needed by the next step. e.g. converting molecules to SDF.
 *
 * TODO REMOVE THIS CLASS
 *
 * @author timbo
 */
public class DatasetServiceExecutorStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(DatasetServiceExecutorStep.class.getName());

    public static final String OPTION_SERVICE_ENDPOINT = StepDefinitionConstants.OPTION_SERVICE_ENDPOINT;

    public static final String VAR_INPUT_DATASET = StepDefinitionConstants.VARIABLE_INPUT_DATASET;
    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {

        statusMessage = MSG_PREPARING_INPUT;

        Dataset<MoleculeObject> dataset = fetchMappedInput(VAR_INPUT_DATASET, Dataset.class, varman);
        String endpoint = getOption(OPTION_SERVICE_ENDPOINT, String.class);

        InputStream is = JsonHandler.getInstance().marshalStreamToJsonArray(dataset.getStream(), false);
//            String inputData = IOUtils.convertStreamToString(input);
//            LOG.info("Input: " + inputData);
//            input = new ByteArrayInputStream(inputData.getBytes());

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");

        // send for execution
        statusMessage = "Posting request ...";
        Map<String, Object> responseHeaders = new HashMap<>();
        InputStream output = CamelUtils.doRequestUsingHeadersAndQueryParams(context, "POST", endpoint, is, requestHeaders, responseHeaders, options);
        statusMessage = "Handling results ...";

//        String data = IOUtils.convertStreamToString(IOUtils.getGunzippedInputStream(output), 1000);
//        LOG.info("Results: " + data);
//        output = new ByteArrayInputStream(data.getBytes());

        createMappedOutput(VAR_OUTPUT_DATASET, InputStream.class, output, varman);

        statusMessage = generateStatusMessage(dataset.getSize(), -1, -1);
    }

    @Override
    public Map<String, Object> executeWithData(Map<String, Object> inputs, CamelContext context) throws Exception {
        throw new RuntimeException("NYI");
    }

}
