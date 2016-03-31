package org.squonk.execution.steps.impl

import com.im.lac.types.BasicObject
import com.im.lac.types.MoleculeObject
import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableClient
import org.squonk.notebook.api.VariableKey
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
    Long producer = 1
    
    void "simple convert"() {
        
        VariableManager varman = new VariableManager(new MemoryVariableClient(),1,1);
        
        varman.putValue(
            new VariableKey(producer,"input"),
            Dataset.class, 
            ds)
        
        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure(producer,
                [(BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME):'struct'],
                [(BasicObjectToMoleculeObjectStep.VAR_INPUT_DATASET):new VariableKey(producer, "input")],
                [:])
        
        when:
        step.execute(varman, null)
        def molsds= varman.getValue(new VariableKey(producer, BasicObjectToMoleculeObjectStep.VAR_OUTPUT_DATASET), Dataset.class)
        
        then:

        molsds != null
        def items = molsds.items
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 2
        items[0].uuid == input[0].uuid
        
    }
    
    
     void "uuid and format props"() {
        
        VariableManager varman = new VariableManager(new MemoryVariableClient(), 1,1);
        
        varman.putValue(
                new VariableKey(producer,"input"),
            Dataset.class, 
            ds)
        
        BasicObjectToMoleculeObjectStep step = new BasicObjectToMoleculeObjectStep()
        step.configure(producer, [
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME):'struct',
                (BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FORMAT):'smiles',
                (BasicObjectToMoleculeObjectStep.OPTION_PRESERVE_UUID):false
            ], [(BasicObjectToMoleculeObjectStep.VAR_INPUT_DATASET) :new VariableKey(producer, "input")],
        [:])
        
        when:
        step.execute(varman, null)
        def molsds = varman.getValue(new VariableKey(producer, BasicObjectToMoleculeObjectStep.VAR_OUTPUT_DATASET), Dataset.class)
        
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

