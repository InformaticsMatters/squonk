package org.squonk.execution.steps

import com.squonk.execution.steps.impl.*
import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import org.squonk.execution.steps.impl.ConvertToIntegerStep
import org.squonk.execution.variable.PersistenceType
import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableLoader
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
        varman.putValue("text", String.class,  "99", PersistenceType.TEXT)
        StepExecutor exec = new StepExecutor(varman);
        
        when:
        exec.execute( [step] as Step[], null)
        Integer intvar = varman.getValue("integer", Integer.class, PersistenceType.TEXT)
        
        then:
        intvar == 99
    }
	
}