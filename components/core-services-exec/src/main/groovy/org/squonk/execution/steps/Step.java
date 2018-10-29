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

import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.squonk.io.IODescriptor;
import org.squonk.core.ServiceDescriptor;
import org.squonk.io.SquonkDataSource;
import org.squonk.notebook.api.VariableKey;

/** Interface for executable steps. A step is executable in 2 ways:
 *
 * 1. External execution where the input data is provided by the client and the results are
 * returned to the client. Execution is handled by the {@link #executeForVariables(Map, CamelContext)}
 * method, with the inputs being present in the first parameter. In this mode the step is configured
 * using the {@link #configure(String jobId, Map options, ServiceDescriptor serviceDescriptor)} method.
 *
 * 2. Notebook execution where the inputs and outputs are notebook variables that are read prior
 * to execution and written following execution. Execution is handled by the
 * {@link #execute(VariableManager, CamelContext)} with the VariableManager being used to read and
 * write the variables. In this mode the step is configured using the
 * {@link #configure(Long producerId, String jobId, Map options, Map inputVariableMappings, Map outputVariableMappings,
 * ServiceDescriptor)} or the
 * {@link #configure(Long producerId, String jobId, Map options,IODescriptor[], IODescriptor[], Map inputVariableMappings,
 * Map outputVariableMappings)} methods. For consistency of execution this mode's should retrieve the input data using the
 * VariableManager, then call the execute() method of the external execution mode and then write the results
 * using the VariableManager.
 *
 *
 * @author timbo
 */
public interface Step {

    /**
     * @return the name of the producer (cell) for the output variables
     */
    Long getOutputProducerId();

    /** For execution in the Squonk notebook where the inputs and outputs are provided as
     * 'variables' that have to be read and written.
     * The inputs and outputs are determined from the ServiceDescriptor
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

    /** For execution in the Squonk notebook where the inputs and outputs are provided as
     * 'variables' that have to be read and written.
     * In this case no ServiceDescriptor is defined so the inputs and outputs are defined explicitly.
     * Typically this means there is no external service to execute so processing is internal to the
     * step ad the input and output types are pre-defined.
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

    /** Configure for basic 'external' execution where the inputs are provided directly to the step/service
     * and the outputs returned to the caller.
     * All parameters related to notebook variables will be null and irrelevant
     *
     * @param jobId
     * @param options
     * @param serviceDescriptor
     */
    void configure(String jobId, Map<String, Object> options, ServiceDescriptor serviceDescriptor);

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

    /** Execute with the given inputs
     * This method is used when executing with externally provided data.
     *
     * @param inputs
     * @return
     * @throws Exception
     */
    Map<String,Object> executeForVariables(Map<String,Object> inputs, CamelContext context) throws Exception;

    Map<String,List<SquonkDataSource>> executeForDataSources(Map<String,Object> inputs, CamelContext context) throws Exception;


    int getNumProcessed();

    int getNumResults();

    int getNumErrors();

    /** Get a message indicating the current status of the execution
     *
     * @return
     */
    String getStatusMessage();

    Map<String, Integer> getUsageStats();

    Map<String, VariableKey> getInputVariableMappings();

    Map<String, String> getOutputVariableMappings();

}
