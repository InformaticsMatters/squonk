package com.squonk.notebook.execution.variable

import com.im.lac.types.*
import com.squonk.dataset.*
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
    
    void "put/get dataset"() {
        
        def objs1 = [
            new BasicObject([id:1,a:"1",hello:'world']),
            new BasicObject([id:2,a:"99",hello:'mars',foo:'bar']),
            new BasicObject([id:3,a:"100",hello:'mum'])
        ]
    
        Dataset ds1 = new Dataset(BasicObject.class, objs1)
        
        MemoryVariableLoader loader = new MemoryVariableLoader()
        VariableManager manager = new VariableManager(loader);
        
        when:
        Variable ds1var = manager.createVariable("ds1", Dataset.class,  ds1, Variable.PersistenceType.DATASET)
        Dataset ds2 = manager.getValue(ds1var)

        
        then:
        manager.getVariables().size() == 1
        loader.values.size() == 2
        ds2 != null
        ds2.items.size() == 3
        
    }
	
}

