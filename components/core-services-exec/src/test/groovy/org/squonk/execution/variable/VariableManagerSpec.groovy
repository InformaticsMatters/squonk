package org.squonk.execution.variable

import com.squonk.execution.variable.impl.*
import com.im.lac.types.*
import com.squonk.dataset.*
import org.squonk.execution.variable.impl.MemoryVariableLoader
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class VariableManagerSpec extends Specification {

    String producer = "p"
    
    void "simple put/get variable"() {
        
        VariableManager manager = new VariableManager(new MemoryVariableLoader());
        
        when:

        manager.putValue(new VariableKey(producer, "text"), String.class,  "John Doe", PersistenceType.TEXT)
        manager.putValue(new VariableKey(producer, "age"), Integer.class, 60, PersistenceType.TEXT)
        
        then:
        manager.getValue(new VariableKey(producer, "text"), String.class, PersistenceType.TEXT) == "John Doe"
        manager.getValue(new VariableKey(producer, "age"), Integer.class, PersistenceType.TEXT) == 60
        
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
        manager.putValue(new VariableKey(producer, "ds1"), Dataset.class,  ds1, PersistenceType.DATASET)
        Dataset ds2 = manager.getValue(new VariableKey(producer, "ds1"), Dataset.class, PersistenceType.DATASET)

        
        then:

        ds2 != null
        ds2.items.size() == 3
        
    }
	
}

