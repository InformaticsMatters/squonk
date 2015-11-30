package com.squonk.notebook.execution.steps;

import com.squonk.notebook.execution.variable.Variable;
import com.squonk.notebook.execution.variable.VariableManager;
import org.apache.camel.CamelContext;

/**
 * Simple step used for testing.
 *
 * @author timbo
 */
public class ConvertToIntegerStep extends AbstractStep {

    public static final String OPTION_SOURCE_FIELD_NAME = "SourceFieldName";
    public static final String OPTION_DESTINATION_FIELD_NAME = "DestinationFieldName";

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
        String srcFieldName = getOption(OPTION_SOURCE_FIELD_NAME, String.class);
        String dstFieldName = getOption(OPTION_DESTINATION_FIELD_NAME, String.class);
        if (srcFieldName == null) {
            throw new IllegalStateException("Option SourceFieldName not defined");
        }
        if (dstFieldName == null) {
            throw new IllegalStateException("Option DestinationFieldName not defined");
        }
        Object input = fetchValue(srcFieldName, Object.class, varman);
        Integer i = new Integer(input.toString());
        createMappedVariable(dstFieldName, Integer.class, i, Variable.PersistenceType.TEXT, varman);
    }

}
