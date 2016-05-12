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
    private final String jobId;
    private volatile Step currentStep;
    
    public StepExecutor(Long  producer, String jobId, VariableManager varman) {
        this.producer = producer;
        this.jobId = jobId;
        this.varman = varman;
    }
    
    public void execute(StepDefinition[] defs, CamelContext context) throws Exception {
        Step[] steps = new Step[defs.length];
        for (int i=0; i<defs.length; i++) {
            StepDefinition def = defs[i];
            Class cls = Class.forName(def.getImplementationClass());
            Step step = (Step)cls.newInstance();
            step.configure(producer, jobId, def.getOptions(), def.getInputVariableMappings(), def.getOutputVariableMappings());
            steps[i] = step;
        }
        execute(steps, context);
    }
    
    public void execute(Step[] steps, CamelContext context) throws Exception {
        for (Step step: steps) {
            currentStep = step;
            step.execute(varman, context);
        }
    }

    public String getCurrentStatus() {
        Step s = currentStep;
        if (s != null) {
            return s.getStatusMessage();
        }
        return null;
    }
    
}
