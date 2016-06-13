package org.squonk.execution.steps.impl

import com.im.lac.types.MoleculeObject
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeServiceFatExecutorStepSpec extends Specification {

    Long producer = 1
    
    void "test simple service"() {

        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext()
        context.start()
               

        Dataset ds = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)
        
        VariableManager varman = new VariableManager(null,1,1)
        varman.putValue(new VariableKey(producer,"input"), Dataset.class, ds)


        def opts = [
            (MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT): "http://localhost:8888/route1"
        ]


        def inputMappings = [(MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET):new VariableKey(producer,"input")]
        def outputMappings = [:]
        
        MoleculeServiceFatExecutorStep step = new MoleculeServiceFatExecutorStep()
        step.configure(producer, "job1", opts, inputMappings, outputMappings)
        
        
        when:
        step.execute(varman, context)
        
        then:
        def output = varman.getValue(new VariableKey(producer, MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET), Dataset.class)
        output instanceof Dataset
        def items = output.items
        items.size() == 3
        items[0].getValue('route1') == 99
        
        cleanup:
        context.stop()
    }
	
}

