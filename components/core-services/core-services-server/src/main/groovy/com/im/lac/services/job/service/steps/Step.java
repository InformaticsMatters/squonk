package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.VariableManager;
import java.util.Map;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public interface Step {

    /**
     * The names of the input variables that are used internally. The actual
     * variable names must match these or be mapped to these.
     *
     * @return The input variable names
     */
    String[] getInputVariableNames();

    /**
     * The names of the output variables that are used internally. The actual
     * variable names must match these or be mapped to these.
     *
     * @return The output variable names
     */
    String[] getOutputVariableNames();

    /**
     * Configure the execution details of the step.
     *
     * @param options Options that configure the execution of the step. e.g. use
     * specified options
     * @param variableMappings Mappings between the variable names provided by
     * the VariableManager and the names expected by the implementation. Keys
     * are the names needed by the implementation, values are the names present
     * in the variable manager.
     */
    void configure(Map<String, Object> options, Map<String, String> variableMappings);

    /**
     * Perform the processing. Each implementation will expect a defined set of
     * input variables and generate a defined set of output variables. These
     * variables are handled through the VariableManager. If the input variable
     * names are not what is expected they can be transformed using the
     * variableMappings.
     *
     * @param varman
     * @param context
     * @throws java.lang.Exception
     */
    void execute(VariableManager varman, CamelContext context) throws Exception;

}
