package org.squonk.execution.steps.impl

import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import com.squonk.dataset.Dataset
import org.squonk.execution.variable.PersistenceType
import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableLoader
import org.squonk.notebook.api.VariableKey
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
        String producer = "p"
        step.configure(producer, [:],
                [(SDFReaderStep.VAR_SDF_INPUT): new VariableKey(producer, "input")],
                [:])
        varman.putValue(
            new VariableKey(producer,"input"),
            InputStream.class, 
            is,
            PersistenceType.BYTES)
        
        when:
        step.execute(varman, null)
        Dataset ds = varman.getValue(new VariableKey(producer, SDFReaderStep.VAR_DATASET_OUTPUT), Dataset.class, PersistenceType.DATASET)
        
        then:
        ds != null
        ds.items.size() == 36

        cleanup:
        is.close()
        
    }
    
	
}

