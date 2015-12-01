package com.squonk.execution.steps

import com.squonk.execution.steps.impl.*
import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
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
        options.put(ConvertToIntegerStep.OPTION_SOURCE_VAR_NAME, "text");
        options.put(ConvertToIntegerStep.OPTION_DESTINATION_VAR_NAME, "integer");
        step.configure(options, null);
        varman.putValue("text", String.class,  "99", Variable.PersistenceType.TEXT)
        StepExecutor exec = new StepExecutor(varman);
        
        when:
        exec.execute( [step] as Step[], null)
        Integer intvar = varman.getValue("integer", Integer.class, Variable.PersistenceType.TEXT)
        
        then:
        intvar == 99
    }
	
}