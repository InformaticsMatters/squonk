package org.squonk.execution.steps;

import org.squonk.execution.variable.VariableManager;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class StepExecutor {
    
    private final VariableManager varman;
    private final Long  producer;
    
    public StepExecutor(Long  producer, VariableManager varman) {
        this.varman = varman;
        this.producer = producer;
    }
    
    public void execute(StepDefinition[] defs, CamelContext context) throws Exception {
        Step[] steps = new Step[defs.length];
        for (int i=0; i<defs.length; i++) {
            StepDefinition def = defs[i];
            Class cls = Class.forName(def.getImplementationClass());
            Step step = (Step)cls.newInstance();
            step.configure(producer, def.getOptions(), def.getInputVariableMappings(), def.getOutputVariableMappings());
            steps[i] = step;
        }
        execute(steps, context);
    }
    
    public void execute(Step[] steps, CamelContext context) throws Exception {
        for (Step step: steps) {
            step.execute(varman, context);
        }
    }
    
}
