package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import org.apache.camel.CamelContext;

/**
 * Simple step used for testing. Creates a new field containing the number of fields
 * present in the VariableManager (prior to addition of the new field).
 *
 * @author timbo
 */
public class VariableCounterStep extends AbstractStep {

    public static final String FIELD_OUTPUT_FIELD_COUNT = "FieldCount";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{FIELD_OUTPUT_FIELD_COUNT};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        int i = varman.getVariables().size();
        createMappedVariable(FIELD_OUTPUT_FIELD_COUNT, Integer.class, i, Variable.PersistenceType.TEXT, varman);
    }

}
