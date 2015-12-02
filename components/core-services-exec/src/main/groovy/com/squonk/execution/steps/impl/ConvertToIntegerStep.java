package com.squonk.execution.steps.impl;

import com.squonk.execution.steps.AbstractStep;
import com.squonk.execution.variable.PersistenceType;
import com.squonk.execution.variable.VariableManager;
import org.apache.camel.CamelContext;

/**
 * Simple step used for testing.
 *
 * @author timbo
 */
public class ConvertToIntegerStep extends AbstractStep {

    public static final String OPTION_SOURCE_VAR_NAME = "SourceVarName";
    public static final String OPTION_DESTINATION_VAR_NAME = "DestinationVarName";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        String srcFieldName = getOption(OPTION_SOURCE_VAR_NAME, String.class);
        String dstFieldName = getOption(OPTION_DESTINATION_VAR_NAME, String.class);
        if (srcFieldName == null) {
            throw new IllegalStateException("Option SourceVarName not defined");
        }
        if (dstFieldName == null) {
            throw new IllegalStateException("Option DestinationVarName not defined");
        }
        String input = fetchValue(srcFieldName, String.class, PersistenceType.TEXT, varman);
        Integer i = new Integer(input);
        createMappedVariable(dstFieldName, Integer.class, i, PersistenceType.TEXT, varman);
    }

}
