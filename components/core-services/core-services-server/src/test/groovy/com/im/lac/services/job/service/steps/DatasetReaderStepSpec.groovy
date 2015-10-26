package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetReaderStepSpec extends Specification {
    
    void "test read mols"() {
        
        def data = '[{"uuid":"e783f703-725c-4cc6-9619-63920ad25e3e","source":"C","format":"smiles"},{"uuid":"ca1e8b1d-bb59-4c51-9b85-810eecc59db1","source":"CC","format":"smiles"},{"uuid":"ec4eecd5-6cd6-438f-b026-eb8add03d7f7","source":"CCC","format":"smiles"}]'
        def meta = '{"type":"com.im.lac.types.MoleculeObject","size":3}'
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        DatasetReaderStep step = new DatasetReaderStep()
        Variable dv = varman.createVariable(DatasetReaderStep.FIELD_INPUT_DATA, String.class, data, true)
        Variable mv = varman.createVariable(DatasetReaderStep.FIELD_INPUT_METADATA, String.class, meta, true)
        
        when:
        step.execute(varman)
        Variable datasetvar = varman.lookupVariable(DatasetReaderStep.FIELD_OUTPUT_DATASET)
        
        then:
        datasetvar != null
        def ds = varman.getValue(datasetvar)
        
        ds.items.size() == 3
	
    }

}