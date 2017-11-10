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

import org.squonk.execution.variable.VariableManager;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.squonk.io.IODescriptor;
import org.squonk.core.ServiceDescriptor;
import org.squonk.notebook.api.VariableKey;

/**
 * @author timbo
 */
public interface Step {

    /**
     * @return the name of the producer (cell) for the output variables
     */
    Long getOutputProducerId();

    /**
     * Configure the execution details of a step that uses a service for execution. The IODescriptors for input and output
     * are determined from the ServiceDescriptor.
     *
     * @param producerId             The cell ID for the producer of output variables
     * @param jobId                  The job ID to associate the work with
     * @param options                Options that configure the execution of the step. e.g. use
     *                               specified options
     * @param inputVariableMappings  Mappings between the variable names provided by
     *                               the VariableManager and the names expected by the implementation. Keys
     *                               are the names needed by the implementation, values are the VariableKeys that
     *                               can be used to fetch the actual values from the variable manager.
     * @param outputVariableMappings The names for the output variables. The producer is determined by {@link #getOutputProducerId}
     * @param serviceDescriptor      Descriptor of the executable service.
     */
    void configure(
            Long producerId,
            String jobId,
            Map<String, Object> options,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings,
            ServiceDescriptor serviceDescriptor);

    /** Configure where there is no external service to execute. The processing is internal to the step ad the input and
     * output types are pre-defined.
     *
     * @param producerId
     * @param jobId
     * @param options
     * @param inputVariableMappings
     * @param outputVariableMappings
     */
    void configure(
            Long producerId,
            String jobId,
            Map<String, Object> options,
            IODescriptor[] inputs,
            IODescriptor[] outputs,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings);

    IODescriptor[] getInputs();
    IODescriptor[] getOutputs();

    /**
     * Perform the processing. Each implementation will expect a defined set of
     * input variables and generate a defined set of output variables. These
     * variables are handled through the VariableManager. If the input variable
     * names are not what is expected they can be transformed using the
     * inputVariableMappings.
     *
     * @param varman
     * @param context
     * @throws java.lang.Exception
     */
    void execute(VariableManager varman, CamelContext context) throws Exception;

    /** Get a message indicating the current status of the execution
     *
     * @return
     */
    String getStatusMessage();

    Map<String, Integer> getUsageStats();

    Map<String, VariableKey> getInputVariableMappings();

    Map<String, String> getOutputVariableMappings();

}
