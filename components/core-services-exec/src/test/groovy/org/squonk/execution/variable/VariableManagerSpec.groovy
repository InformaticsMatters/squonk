package org.squonk.execution.variable

import com.im.lac.types.*
import org.squonk.dataset.Dataset
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class VariableManagerSpec extends Specification {

    Long producer = 1l
    
    void "simple put/get variable"() {

        VariableManager manager = new VariableManager(null, 1, 1);

        when:

        manager.putValue(new VariableKey(producer, "text"), String.class, "John Doe")
        manager.putValue(new VariableKey(producer, "age"), Integer.class, new Integer(60))

        then:
        manager.getValue(new VariableKey(producer, "text"), String.class) == "John Doe"
        manager.getValue(new VariableKey(producer, "age"), Integer.class) == 60

    }

    void "put/get dataset"() {

        def objs1 = [
            new BasicObject([id:1,a:"1",hello:'world']),
            new BasicObject([id:2,a:"99",hello:'mars',foo:'bar']),
            new BasicObject([id:3,a:"100",hello:'mum'])
        ]

        Dataset ds1 = new Dataset(BasicObject.class, objs1)

        VariableManager manager = new VariableManager(null, 1, 1);

        when:
        manager.putValue(new VariableKey(producer, "ds1"), Dataset.class, ds1)
        Dataset ds2 = manager.getValue(new VariableKey(producer, "ds1"), Dataset.class)


        then:

        ds2 != null
        ds2.items.size() == 3

    }
	
}

