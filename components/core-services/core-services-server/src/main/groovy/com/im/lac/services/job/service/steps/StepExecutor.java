package com.im.lac.services.job.service.steps;

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
    
    public void execute(Step[] steps, CamelContext context) throws Exception {
        for (Step step: steps) {
            step.execute(varman, context);
        }
        // VariableManager should be transactional?
        varman.save();
    }
    
}
