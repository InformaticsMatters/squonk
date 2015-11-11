package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import com.squonk.dataset.Dataset
import com.squonk.types.SDFile
import com.squonk.util.IOUtils
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class SDFReaderStepSpec extends Specification {
    
    void "test read sdf"() {
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        SDFReaderStep step = new SDFReaderStep()
        FileInputStream is = new FileInputStream("../../../data/testfiles/Kinase_inhibs.sdf.gz")
        Variable sdf = varman.createVariable(
            SDFReaderStep.VAR_SDF_INPUT, 
            InputStream.class, 
            is,
            Variable.PersistenceType.BYTES)
        
        when:
        step.execute(varman, null)
        Variable molsvar = varman.lookupVariable(SDFReaderStep.VAR_DATASET_OUTPUT)
        
        then:
        molsvar != null
        Object ds = varman.getValue(molsvar)
        ds != null
        ds instanceof Dataset
        ds.items.size() == 36
        
        
        cleanup:
        is.close()
        
    }
    
	
}

