package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class StepExecutorSpec extends Specification {
    
    void "simple step execution"() {
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        ConvertToIntegerStep step = new ConvertToIntegerStep()
        Map<String, Object> options = new HashMap<>();
        options.put(ConvertToIntegerStep.OPTION_SOURCE_FIELD_NAME, "text");
        options.put(ConvertToIntegerStep.OPTION_DESTINATION_FIELD_NAME, "integer");
        step.configure(options, null);
        Variable text = varman.createVariable("text", String.class,  "99", Variable.PersistenceType.TEXT)
        StepExecutor exec = new StepExecutor(varman);
        
        when:
        exec.execute( [step] as Step[])
        Variable intvar = varman.lookupVariable("integer")
        
        then:
        varman.getVariables().size() == 2
        intvar != null
        varman.getValue(intvar) == 99
    }
	
}

