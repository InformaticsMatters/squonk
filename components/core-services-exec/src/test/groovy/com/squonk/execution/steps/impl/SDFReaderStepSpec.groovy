package com.squonk.execution.steps.impl

import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import com.squonk.dataset.Dataset
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class SDFReaderStepSpec extends Specification {
    
    void "test read sdf"() {
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        SDFReaderStep step = new SDFReaderStep()
        FileInputStream is = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        varman.putValue(
            SDFReaderStep.VAR_SDF_INPUT, 
            InputStream.class, 
            is,
            PersistenceType.BYTES)
        
        when:
        step.execute(varman, null)
        Dataset ds = varman.getValue(SDFReaderStep.VAR_DATASET_OUTPUT, Dataset.class, PersistenceType.DATASET)
        
        then:
        ds != null
        ds.items.size() == 36

        cleanup:
        is.close()
        
    }
    
	
}

