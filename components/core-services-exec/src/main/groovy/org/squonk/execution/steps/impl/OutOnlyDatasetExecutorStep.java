/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

import org.squonk.dataset.Dataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.types.io.JsonHandler;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.logging.Logger;

/**Thin executor either has no body or gets its body from the option whose key is 'body'
 * This is POSTed to the service and a Dataset is returned
 *
 *
 * @author timbo
 */
public class OutOnlyDatasetExecutorStep extends AbstractDatasetStep {

    private static final Logger LOG = Logger.getLogger(OutOnlyDatasetExecutorStep.class.getName());

    public static final String OPTION_SERVICE_ENDPOINT = StepDefinitionConstants.OPTION_SERVICE_ENDPOINT;

    public static final String VAR_OUTPUT_DATASET = StepDefinitionConstants.VARIABLE_OUTPUT_DATASET;

    public OutOnlyDatasetExecutorStep() {
        inputRequired = false;
    }

    /**
     *
     * @param input The parameter is expected to be null and is ignored
     * @return
     * @throws Exception
     */
    @Override
    protected Dataset doExecuteWithDataset(Dataset input) throws Exception {
        statusMessage = MSG_PREPARING_INPUT;

        String endpoint = getHttpExecutionEndpoint();
        Object body = getOption(StepDefinitionConstants.OPTION_BODY);
        String bodyContentType = getOption(StepDefinitionConstants.OPTION_BODY_CONTENT_TYPE, String.class);

        String query = null;
        if (body != null) {
            LOG.info("Body type: " + body.getClass().getName());
            if (body instanceof String) {
                query = (String)body;
            } else {
                query = JsonHandler.getInstance().objectToJson(body);
            }
        }
        LOG.info("Input: " + query);

        Dataset results = handleHttpPost(camelContext, endpoint, new ByteArrayInputStream(query.getBytes()));
        return results;
    }

}
