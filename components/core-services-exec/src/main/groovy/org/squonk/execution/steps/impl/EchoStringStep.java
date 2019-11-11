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

import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.execution.steps.AbstractStep;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/** Simple step used for testing that reads text input and writes it to output
 *
 * Created by timbo on 06/01/16.
 */
public class EchoStringStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(EchoStringStep.class.getName());

    public static final DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor("core.dataset.sorter.v1",
            "Echo string",
            "Echo a string from the input to the output",
            new String[]{"echo", "string"},
            null, "icons/filter.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createStringArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createStringArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            null , null, null, null,
            EchoStringStep.class.getName()
    );

    @Override
    public Map<String, Object> doExecute(Map<String, Object> inputs) throws Exception {
        return Collections.singletonMap("output", inputs.get("input"));
    }
}
