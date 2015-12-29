package org.squonk.execution.steps.impl

import org.squonk.dataset.Dataset
import org.squonk.execution.variable.PersistenceType
import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableLoader
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CSVReaderStepSpec extends Specification {
    
    static String CSV1 = '''\
field1,field2,field3
1,one,uno
2,two,duo
3,three,tres'''
    
    static String TAB1 = '''\
field1\tfield2\tfield3
1\tone\tuno
2\ttwo\tduo
3\tthree\ttres'''
    
    void "simple csv reader with header"() {
        //println "simple csv reader with header"
        InputStream is = new ByteArrayInputStream(CSV1.bytes)
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        String producer = "p"
        varman.putValue(
                new VariableKey(producer, "input"),
            InputStream.class, 
            is,
            PersistenceType.BYTES)
        
        
        CSVReaderStep step = new CSVReaderStep()
        step.configure(producer, [
                (CSVReaderStep.OPTION_FORMAT_TYPE):'DEFAULT',
                (CSVReaderStep.OPTION_USE_HEADER_FOR_FIELD_NAMES):true,
                (CSVReaderStep.OPTION_SKIP_HEADER_LINE):true
            ], [(CSVReaderStep.VAR_CSV_INPUT):new VariableKey(producer, "input")], [:])
        
        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, CSVReaderStep.VAR_DATASET_OUTPUT), Dataset.class, PersistenceType.DATASET)
        
        then:
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 3
    }
    
    void "simple tab reader without header"() {
        //println "simple tab reader without header"
        InputStream is = new ByteArrayInputStream(TAB1.bytes)
        String producer = "p"
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        varman.putValue(
                new VariableKey(producer, "input"),
                InputStream.class,
                is,
                PersistenceType.BYTES)
        
        
        CSVReaderStep step = new CSVReaderStep()

        step.configure(producer,
                [(CSVReaderStep.OPTION_FORMAT_TYPE):'TDF'],
                [(CSVReaderStep.VAR_CSV_INPUT):new VariableKey(producer, "input")],
                [:])
        
        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, CSVReaderStep.VAR_DATASET_OUTPUT), Dataset.class, PersistenceType.DATASET)
        
        then:
        dataset != null
        def items = dataset.items
        items.size() == 4
        items[0].values.size() == 3
    }
	
}

