package com.squonk.execution.variable

import com.squonk.execution.variable.impl.*
import com.squonk.execution.steps.*
import com.squonk.execution.steps.impl.*
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
        manager.putValue("text", String.class,  "John Doe", Variable.PersistenceType.TEXT)
        manager.putValue("age", Integer.class, 60, Variable.PersistenceType.TEXT)
        
        then:
        manager.getValue("text", String.class, Variable.PersistenceType.TEXT) == "John Doe"
        manager.getValue("age", Integer.class, Variable.PersistenceType.TEXT) == 60
        
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
        manager.putValue("ds1", Dataset.class,  ds1, Variable.PersistenceType.DATASET)
        Dataset ds2 = manager.getValue("ds1", Dataset.class, Variable.PersistenceType.DATASET)

        
        then:

        ds2 != null
        ds2.items.size() == 3
        
    }
	
}

