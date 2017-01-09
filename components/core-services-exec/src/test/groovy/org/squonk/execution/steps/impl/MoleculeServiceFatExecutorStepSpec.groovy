package org.squonk.execution.steps.impl

import org.squonk.core.HttpServiceDescriptor
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.types.MoleculeObject
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

        def inputMappings = ["input":new VariableKey(producer,"input")]
        def outputMappings = [:]

        HttpServiceDescriptor sd = new HttpServiceDescriptor("id.busybox", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", 'http://localhost:8888/route1')
        
        MoleculeServiceFatExecutorStep step = new MoleculeServiceFatExecutorStep()
        step.configure(producer, "job1", null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[], inputMappings, outputMappings, sd)
        
        
        when:
        step.execute(varman, context)
        
        then:
        def output = varman.getValue(new VariableKey(producer, "output"), Dataset.class)
        output instanceof Dataset
        def items = output.items
        items.size() == 3
        items[0].getValue('route1') == 99
        
        cleanup:
        context.stop()
    }
	
}

