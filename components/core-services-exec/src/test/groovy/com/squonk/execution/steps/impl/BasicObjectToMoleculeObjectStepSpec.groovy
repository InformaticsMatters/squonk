package com.squonk.execution.steps.impl

import com.squonk.execution.variable.impl.*
import com.squonk.execution.variable.*
import com.im.lac.types.BasicObject
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class BasicObjectToMoleculeObjectStepSpec extends Specification {
    
    def input = [
        new BasicObject([struct:"C",   num:1, hello:'mercury']),
        new BasicObject([struct:"CC",  num:2, hello:'venus']),
        new BasicObject([struct:"CCC", num:3, hello:'world']),
    ]
    Dataset ds = new Dataset(BasicObject.class, input)
    
    void "simple convert"() {
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        
        varman.putValue(
            BasicObjectToMoleculeObjectStep.VAR_INPUT_DATASET, 
            Dataset.class, 
            ds,
            Variable.PersistenceType.NONE)
        
        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure([(BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME):'struct'], [:])
        
        when:
        step.execute(varman, null)
        def molsds= varman.getValue(BasicObjectToMoleculeObjectStep.VAR_OUTPUT_DATASET, Dataset.class, Variable.PersistenceType.DATASET)
        
        then:

        molsds != null
        def items = molsds.items
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 2
        items[0].uuid == input[0].uuid
        
    }
    
    
     void "uuid and format props"() {
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        
        varman.putValue(
            BasicObjectToMoleculeObjectStep.VAR_INPUT_DATASET, 
            Dataset.class, 
            ds,
            Variable.PersistenceType.NONE)
        
        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure([
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME):'struct',
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FORMAT):'smiles',
                (BasicObjectToMoleculeObjectStep.OPTION_PRESERVE_UUID):false
            ], [:])
        
        when:
        step.execute(varman, null)
        def molsds = varman.getValue(BasicObjectToMoleculeObjectStep.VAR_OUTPUT_DATASET, Dataset.class, Variable.PersistenceType.DATASET)
        
        then:

        molsds != null
        def items = molsds.items
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 2
        items[0].uuid != input[0].uuid
        items[0].format == 'smiles'
        
    }
    
}

