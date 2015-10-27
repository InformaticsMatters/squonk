package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
import com.squonk.dataset.DatasetMetadata
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetReaderStepSpec extends Specification {
    
    void "test read mols"() {
        
        def mols = [
            new MoleculeObject("C", "smiles"),
            new MoleculeObject("CC", "smiles"),
            new MoleculeObject("CCC", "smiles")
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        def meta = new DatasetMetadata(MoleculeObject.class, null, 3)
        
        MemoryVariableLoader loader = new MemoryVariableLoader()
        VariableManager varman = new VariableManager(loader)
        DatasetReaderStep step = new DatasetReaderStep()
        Variable dv = varman.createVariable(DatasetReaderStep.FIELD_INPUT_DATA, InputStream.class, ds.getInputStream(false), Variable.PersistenceType.BYTES)
        Variable mv = varman.createVariable(DatasetReaderStep.FIELD_INPUT_METADATA, DatasetMetadata.class, meta, Variable.PersistenceType.JSON)
        
        when:
        step.execute(varman, null)
        Variable datasetvar = varman.lookupVariable(DatasetReaderStep.FIELD_OUTPUT_DATASET)
        
        then:
        datasetvar != null
        def result = varman.getValue(datasetvar)
        
        result.items.size() == 3
	
    }

}