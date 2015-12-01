package com.squonk.execution.steps.impl

import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
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
            DatasetWriterStep.VAR_INPUT_DATASET, 
            Dataset.class, 
            ds,
            Variable.PersistenceType.NONE)
        
        when:
        step.execute(varman, null)
        Variable outvar = varman.lookupVariable(DatasetWriterStep.VAR_OUTPUT_DATASET)
        
        then:
        outvar != null
        def dataset = varman.getValue(outvar)
        dataset != null
	
    }

}