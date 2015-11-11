package com.im.lac.services.job.service.steps

import com.im.lac.services.job.variable.*
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.*
import com.squonk.dataset.transform.TransformDefintions
import java.util.stream.Stream
import org.apache.camel.impl.DefaultCamelContext
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
        
        Variable dsvar = varman.createVariable(
            ValueTransformerStep.VAR_INPUT_DATASET, 
            Dataset.class, 
            ds,
            Variable.PersistenceType.NONE)
        
        ValueTransformerStep step = new ValueTransformerStep()
        step.configure([(ValueTransformerStep.OPTION_TRANSFORMS):tdefs], [:])
        
        when:
        step.execute(varman, context)
        Variable datasetvar = varman.lookupVariable(ValueTransformerStep.VAR_INPUT_DATASET)
        
        then:
        datasetvar != null
        def dataset = varman.getValue(datasetvar)
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