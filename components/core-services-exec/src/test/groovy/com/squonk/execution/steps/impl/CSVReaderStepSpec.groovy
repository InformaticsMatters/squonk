package com.squonk.execution.steps.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import com.im.lac.types.BasicObject
import java.util.stream.Stream
import org.apache.commons.csv.CSVFormat
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
        Variable csv = varman.createVariable(
            CSVReaderStep.VAR_CSV_INPUT, 
            InputStream.class, 
            is,
            Variable.PersistenceType.BYTES)
        
        
        CSVReaderStep step = new CSVReaderStep()
        step.configure([
                (CSVReaderStep.OPTION_FORMAT_TYPE):'DEFAULT',
                (CSVReaderStep.OPTION_USE_HEADER_FOR_FIELD_NAMES):true,
                (CSVReaderStep.OPTION_SKIP_HEADER_LINE):true
            ], [:])
        
        when:
        step.execute(varman, null)
        Variable datasetvar = varman.lookupVariable(CSVReaderStep.VAR_DATASET_OUTPUT)
        
        then:
        datasetvar != null
        def dataset = varman.getValue(datasetvar)
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 3
    }
    
    void "simple tab reader without header"() {
        //println "simple tab reader without header"
        InputStream is = new ByteArrayInputStream(TAB1.bytes)
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        Variable csv = varman.createVariable(
            CSVReaderStep.VAR_CSV_INPUT, 
            InputStream.class, 
            is,
            Variable.PersistenceType.BYTES)
        
        
        CSVReaderStep step = new CSVReaderStep()
        step.configure([
                (CSVReaderStep.OPTION_FORMAT_TYPE):'TDF'
            ], [:])
        
        when:
        step.execute(varman, null)
        Variable datasetvar = varman.lookupVariable(CSVReaderStep.VAR_DATASET_OUTPUT)
        
        then:
        datasetvar != null
        def dataset = varman.getValue(datasetvar)
        dataset != null
        def items = dataset.items
        items.size() == 4
        items[0].values.size() == 3
    }
	
}

