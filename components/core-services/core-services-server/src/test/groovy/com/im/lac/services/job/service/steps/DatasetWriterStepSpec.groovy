package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
import java.util.stream.Stream
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetWriterStepSpec extends Specification {
    
    void "test write mols"() {
        
        def mols = [
            new MoleculeObject("C", "smiles"),
            new MoleculeObject("CC", "smiles"),
            new MoleculeObject("CCC", "smiles")
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        DatasetWriterStep step = new DatasetWriterStep()
        Variable dsvar = varman.createVariable(
            DatasetWriterStep.FIELD_INPUT_DATASET, 
            Dataset.class, 
            ds,
            Variable.PersistenceType.NONE)
        
        when:
        step.execute(varman, null)
        Variable outvar = varman.lookupVariable(DatasetWriterStep.FIELD_OUTPUT_DATASET)
        
        then:
        outvar != null
        def dataset = varman.getValue(outvar)
        dataset != null

        
        
	
    }

}