package com.squonk.execution.steps.impl;

import com.squonk.execution.steps.AbstractStep;
import com.squonk.execution.variable.Variable;
import com.squonk.execution.variable.VariableManager;
import org.apache.camel.CamelContext;

/**
 * Simple step used for testing. Creates a new field containing the number of fields
 present in the VariableManager1 (prior to addition of the new field).
 *
 * @author timbo
 */
public class VariableCounterStep extends AbstractStep {

    public static final String VAR_OUTPUT_FIELD_COUNT = "_VariableCounterFieldCount";

    @Override
    public String[] getInputVariableNames() {
        return new String[]{};
    }

    @Override
    public String[] getOutputVariableNames() {
        return new String[]{VAR_OUTPUT_FIELD_COUNT};
    }

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        int i = varman.getVariables().size();
        createMappedVariable(VAR_OUTPUT_FIELD_COUNT, Integer.class, i, Variable.PersistenceType.TEXT, varman);
    }

}
