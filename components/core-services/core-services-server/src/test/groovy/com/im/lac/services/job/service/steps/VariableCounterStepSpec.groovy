package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class VariableCounterStepSpec extends Specification {
	
    
    void "single step"() {
        
        MemoryVariableLoader loader = new MemoryVariableLoader()
        VariableManager varman = new VariableManager(loader)
        VariableCounterStep step = new VariableCounterStep()
        
        StepExecutor exec = new StepExecutor(varman);
        
        when:
        exec.execute( [step] as Step[], null)  
        
        then:
        Variable var = varman.lookupVariable(VariableCounterStep.FIELD_OUTPUT_FIELD_COUNT)
        var != null
        varman.getValue(var) == 0
    }
    
    void "two steps fail without mapping"() {
        
        MemoryVariableLoader loader = new MemoryVariableLoader()
        VariableManager varman = new VariableManager(loader)
        VariableCounterStep step1 = new VariableCounterStep()
        VariableCounterStep step2 = new VariableCounterStep()
        
        StepExecutor exec = new StepExecutor(varman);
        
        when:
        exec.execute( [step1, step2] as Step[], null)  
        
        then:
        thrown(IllegalStateException)
    }
    
    void "two steps ok with mapping"() {
        
        MemoryVariableLoader loader = new MemoryVariableLoader()
        VariableManager varman = new VariableManager(loader)
        VariableCounterStep step1 = new VariableCounterStep()
        step1.configure([:], [(VariableCounterStep.FIELD_OUTPUT_FIELD_COUNT):'count1'])
        VariableCounterStep step2 = new VariableCounterStep()
        step2.configure([:], [(VariableCounterStep.FIELD_OUTPUT_FIELD_COUNT):'count2'])
        
        StepExecutor exec = new StepExecutor(varman);
        
        when:
        exec.execute( [step1, step2] as Step[], null)  
        
        then:
        Variable var1 = varman.lookupVariable('count1')
        var1 != null
        varman.getValue(var1) == 0
        Variable var2 = varman.lookupVariable('count2')
        var2 != null
        varman.getValue(var2) == 1
    }
    
}

