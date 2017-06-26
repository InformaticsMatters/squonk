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

package org.squonk.execution.steps;

import org.squonk.core.ServiceDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;

import java.util.Map;

/**
 * Created by timbo on 20/06/17.
 */
public abstract class AbstractStandardStep extends AbstractStep {

    protected IODescriptor[] inputs;
    protected IODescriptor[] outputs;


    @Override
    public IODescriptor[] getInputs() {
        return inputs;
    }
    @Override
    public IODescriptor[] getOutputs() {
        return outputs;
    }


    @Override
    public void configure(
            Long outputProducerId,
            String jobId,
            Map<String, Object> options,
            IODescriptor[] inputs,
            IODescriptor[] outputs,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings) {
        this.outputProducerId = outputProducerId;
        this.jobId = jobId;
        this.options = options;
        this.inputs = inputs;
        this.outputs = outputs;
        this.inputVariableMappings.putAll(inputVariableMappings);
        this.outputVariableMappings.putAll(outputVariableMappings);

    }

    @Override
    public void configure(
            Long outputProducerId,
            String jobId,
            Map<String, Object> options,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings,
            ServiceDescriptor serviceDescriptor) {
        throw new IllegalStateException(this.getClass().getCanonicalName() + " does not support ServiceDescriptors");
    }
}
