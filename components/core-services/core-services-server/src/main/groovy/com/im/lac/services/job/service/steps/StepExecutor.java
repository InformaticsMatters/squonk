package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.VariableManager;

/**
 *
 * @author timbo
 */
public class StepExecutor {
    
    private final VariableManager varman;
    
    public StepExecutor(VariableManager varman) {
        this.varman = varman;
    }
    
    public void execute(Step[] steps) throws Exception {
        for (Step step: steps) {
            step.execute(varman);
        }
        // VariableManager should be transactional?
        varman.save();
    }
    
}
