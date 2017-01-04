package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.io.IODescriptor
import org.squonk.dataset.Dataset
import org.squonk.execution.docker.DockerExecutorDescriptor
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.notebook.api.VariableKey
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 13/09/16.
 */
class DefaultDockerExecutorStepSpec extends Specification {


    Long producer = 1

    def createDataset() {
        def mols = [
                new MoleculeObject('C', 'smiles', [idx: 0, a: 11, b: 'red',    c: 7, d: 5]),
                new MoleculeObject('CC', 'smiles', [idx: 1, a: 23, b: 'blue',   c: 5]),
                new MoleculeObject('CCC', 'smiles', [idx: 2, a: 7,  b: 'green',  c: 5, d: 7]),
                new MoleculeObject('CCCC', 'smiles', [idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    def createVariableManager(varname) {
        VariableManager varman = new VariableManager(null, 1, 1);
        varman.putValue(
                new VariableKey(producer, varname),
                Dataset.class,
                createDataset())
        return varman
    }

    def createStep(args, cmd, inputRead, inputWrite, outputRead, outputWrite) {
        DockerExecutorDescriptor ded = new DockerExecutorDescriptor("id.busybox", "name", "desc",  null, null, null,
                [IODescriptors.createMoleculeObjectDataset(inputWrite)] as IODescriptor[], [IORoute.FILE] as IORoute[],
                [IODescriptors.createMoleculeObjectDataset(outputRead)] as IODescriptor[], [IORoute.FILE] as IORoute[],
                null, "executor", 'busybox', cmd, [:])

        DefaultDockerExecutorStep step = new DefaultDockerExecutorStep()
        step.configure(producer, "job1",
                args,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                [(inputWrite): new VariableKey(producer, inputRead)],
                [(outputRead): outputWrite],
                ded
        )
        return step
    }


    void "simple execute using json"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager("input_v")
        Map args = ['docker.executor.id' :'id.busybox']
        DefaultDockerExecutorStep step = createStep(args, 'cp input_d.data.gz output_d.data.gz && cp input_d.metadata output_d.metadata', "input_v", "input_d", "output_d", "output_v")

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, "output_v"), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4

    }

    // need a way to do conversions without the CDK web service
//    void "execute using sdf"() {
//
//        DefaultCamelContext context = new DefaultCamelContext()
//        VariableManager varman = createVariableManager()
//        Map args = ['docker.executor.id' :'id.busybox']
//        CannedDockerProcessDatasetStep step = createStep(args,'cp input.sdf.gz output.sdf.gz',  CommonMimeTypes.MIME_TYPE_MDL_SDF,  CommonMimeTypes.MIME_TYPE_MDL_SDF)
//
//        when:
//        step.execute(varman, context)
//        Dataset dataset = varman.getValue(new VariableKey(producer, StepDefinitionConstants.VARIABLE_OUTPUT_DATASET), Dataset.class)
//
//        then:
//        dataset != null
//        dataset.generateMetadata()
//        List results = dataset.getItems()
//        results.size() == 4
//
//    }
}