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
            DatasetWriterStep.FIELD_SOURCE_DATASET, 
            Dataset.class, 
            ds,
            Variable.PersistenceType.NONE)
        
        when:
        step.execute(varman, null)
        Variable datavar = varman.lookupVariable(DatasetWriterStep.FIELD_OUTPUT_DATA)
        Variable metavar = varman.lookupVariable(DatasetWriterStep.FIELD_OUTPUT_METADATA)
        
        then:
        datavar != null
        metavar != null
        def data = varman.getValue(datavar)
        def meta = varman.getValue(metavar)
        
        println "DATA: $data"
        println "META:  $meta"
        
        
	
    }

}