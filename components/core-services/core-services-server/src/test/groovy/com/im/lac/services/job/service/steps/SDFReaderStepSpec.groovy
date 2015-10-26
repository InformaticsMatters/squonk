package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import com.squonk.dataset.MoleculeObjectDataset
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
            SDFReaderStep.FIELD_SDF_INPUT, 
            SDFile.class, 
            new SDFile(is),
            true)
        
        when:
        step.execute(varman)
        Variable molsvar = varman.lookupVariable(SDFReaderStep.FIELD_DATASET_OUTPUT)
        
        then:
        molsvar != null
        Object ds = varman.getValue(molsvar)
        ds != null
        ds instanceof MoleculeObjectDataset
        ds.items.size() == 36
        
        
        cleanup:
        is.close()
        
    }
    
	
}

