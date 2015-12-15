package org.squonk.execution.steps.impl

import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
import org.squonk.execution.variable.PersistenceType
import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableLoader
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
        varman.putValue(
            DatasetWriterStep.VAR_INPUT_DATASET, 
            Dataset.class, 
            ds,
            PersistenceType.NONE)
        
        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(DatasetWriterStep.VAR_OUTPUT_DATASET, Dataset.class, PersistenceType.DATASET)
        
        then:
        dataset != null
	
    }

}