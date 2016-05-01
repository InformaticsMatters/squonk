package org.squonk.execution.steps;

import org.squonk.execution.variable.VariableManager;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.squonk.notebook.api.VariableKey;

/**
 * @author timbo
 */
public interface Step {

    /**
     * @return the name of the producer for the output variables
     */
    Long getOutputProducerId();

    /**
     * Configure the execution details of the step.
     *
     * @param producerId             The cell ID for the producer of output variables
     * @param options                Options that configure the execution of the step. e.g. use
     *                               specified options
     * @param inputVariableMappings  Mappings between the variable names provided by
     *                               the VariableManager and the names expected by the implementation. Keys
     *                               are the names needed by the implementation, values are the VariableKeys that
     *                               can be used to fetch the actual values from the variable manager.
     * @param outputVariableMappings The names for the output variables. The producer is determined by {@link #getOutputProducerId}
     */
    void configure(Long producerId, Map<String, Object> options, Map<String, VariableKey> inputVariableMappings, Map<String, String> outputVariableMappings);

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

}
