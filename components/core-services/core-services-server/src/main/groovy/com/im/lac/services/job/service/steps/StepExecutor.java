package com.im.lac.services.job.service.steps;

import com.im.lac.job.jobdef.StepDefinition;
import com.im.lac.services.job.variable.VariableManager;
import org.apache.camel.CamelContext;

/**
 *
 * @author timbo
 */
public class StepExecutor {
    
    private final VariableManager varman;
    
    public StepExecutor(VariableManager varman) {
        this.varman = varman;
    }
    
    public void execute(StepDefinition[] defs, CamelContext context) throws Exception {
        Step[] steps = new Step[defs.length];
        for (int i=0; i<defs.length; i++) {
            StepDefinition def = defs[i];
            Class cls = Class.forName(def.getImplementationClass());
            Step step = (Step)cls.newInstance();
            step.configure(def.getOptions(), def.getFieldMappings());
            steps[i] = step;
        }
        execute(steps, context);
    }
    
    public void execute(Step[] steps, CamelContext context) throws Exception {
        for (Step step: steps) {
            step.execute(varman, context);
        }
        // VariableManager should be transactional?
        varman.save();
    }
    
}
