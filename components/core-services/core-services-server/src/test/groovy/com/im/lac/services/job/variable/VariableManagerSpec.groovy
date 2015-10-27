package com.im.lac.services.job.variable

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class VariableManagerSpec extends Specification {
    
    void "simple put/get variable"() {
        
        VariableManager manager = new VariableManager(new MemoryVariableLoader());
        
        when:
        Variable text = manager.createVariable("text", String.class,  "John Doe", Variable.PersistenceType.TEXT)
        Variable age = manager.createVariable("age", Integer.class, 60, Variable.PersistenceType.TEXT)
        
        then:
        manager.getVariables().size() == 2
        manager.getVariables().contains(text)
        manager.getValue(text) == "John Doe"
        manager.getValue(age) == 60
        
    }
	
}

