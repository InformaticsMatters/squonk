package org.squonk.execution.steps.impl

import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
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
class MoleculeServiceFatExecutorStepSpec extends Specification {
    String producer = "p"
    
    void "test simple service"() {
        
        DefaultCamelContext context = new DefaultCamelContext()
        context.start()
               
        def mols = [
            new MoleculeObject("C", "smiles"),
            new MoleculeObject("CC", "smiles"),
            new MoleculeObject("CCC", "smiles")
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader())
        varman.putValue(
                new VariableKey(producer,"input"), Dataset.class, ds, PersistenceType.NONE)

                //MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET, Dataset.class, ds, PersistenceType.NONE)
        
        def opts = [
            (MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-cdk-basic/rest/v1/calculators/logp'
        ]
        def inputMappings = [(MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET):new VariableKey(producer,"input")]
        def outputMappings = [:]
        
        MoleculeServiceFatExecutorStep step = new MoleculeServiceFatExecutorStep()
        step.configure(producer, opts, inputMappings, outputMappings)
        
        
        when:
        step.execute(varman, context)
        
        then:
        def output = varman.getValue(new VariableKey(producer, MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET), Dataset.class, PersistenceType.DATASET)
        output instanceof Dataset
        def items = output.items
        items.size() == 3
        items[0].getValue('CDK_ALogP') != null
        
        cleanup:
        context.stop()
    }
	
}

