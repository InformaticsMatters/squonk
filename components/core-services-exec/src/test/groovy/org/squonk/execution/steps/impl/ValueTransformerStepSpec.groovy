package org.squonk.execution.steps.impl

import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.*
import com.squonk.dataset.transform.TransformDefintions
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.execution.variable.PersistenceType
import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableLoader
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ValueTransformerStepSpec extends Specification {
    
    void "test write mols"() {
        
        DefaultCamelContext context = new DefaultCamelContext()
        
        def mols = [
            new MoleculeObject("C", "smiles", [num:"1",hello:'world']),
            new MoleculeObject("CC", "smiles", [num:"99",hello:'mars',foo:'bar']),
            new MoleculeObject("CCC", "smiles", [num:"100",hello:'mum'])
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        
        TransformDefintions tdefs = new TransformDefintions()
        .deleteField("foo")
        .renameField("hello", "goodbye")
        .convertField("num", Integer.class);
        
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        String producer = "p"
        varman.putValue(
            new VariableKey(producer, "input"),
            Dataset.class, 
            ds,
            PersistenceType.NONE)
        
        ValueTransformerStep step = new ValueTransformerStep()
        step.configure(producer,
                [(ValueTransformerStep.OPTION_TRANSFORMS):tdefs],
                [(ValueTransformerStep.VAR_INPUT_DATASET):new VariableKey(producer, "input")],
                [:])
        
        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, ValueTransformerStep.VAR_OUTPUT_DATASET), Dataset.class, PersistenceType.DATASET)
        
        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 3 
        DatasetMetadata md = dataset.metadata
        md != null
        md.valueClassMappings.size() == 2
        md.valueClassMappings['num'] == Integer.class
        md.valueClassMappings['goodbye'] == String.class
        
    }

}