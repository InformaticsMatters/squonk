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

package org.squonk.execution.steps;

import org.apache.camel.CamelContext;
import org.squonk.core.ServiceDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;

import java.util.Map;

/** Interface for executable steps.
 *
 * The input data is provided by the caller and the results are
 * returned to the caller. Execution is handled by the {@link #execute(Map)}
 * method, with the inputs being present in the first parameter. The step is configured
 * using the {@link #configure(String, Map, ServiceDescriptor, CamelContext, String)} method.
 *
 *
 * @author timbo
 */
public interface Step {


    /** Configure for basic 'external' execution where the inputs are provided directly to the step/service
     * and the outputs returned to the caller.
     * All parameters related to notebook variables will be null and irrelevant
     *
     * @param jobId
     * @param options
     * @param serviceDescriptor
     * @param context
     * @param auth Authorization token
     */
    void configure(String jobId, Map<String, Object> options, ServiceDescriptor serviceDescriptor, CamelContext context, String auth);

    IODescriptor[] getInputs();
    IODescriptor[] getOutputs();

    /** Execute with the given inputs
     * This method is used when executing with externally provided data.
     *
     * @param inputs
     * @return
     * @throws Exception
     */
    Map<String,Object> execute(Map<String,Object> inputs) throws Exception;

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

    /** Execution clean-up.
     *
     * This method must not block and, as
     * handling errors that occur while cleaning up or closing
     * is extremely difficult, the method MUST NOT throw any exceptions.
     */
    void cleanup();

}
