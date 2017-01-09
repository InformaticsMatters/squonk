package org.squonk.execution.steps.impl

import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.core.HttpServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.steps.Step
import org.squonk.execution.steps.StepExecutor
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.notebook.api.VariableKey
import org.squonk.types.MoleculeObject
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeServiceThinExecutorStepSpec extends Specification {


    void "test routes simple"() {

        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)

        ProducerTemplate pt = context.createProducerTemplate()


        when:
        InputStream result = pt.requestBody("http4:localhost:8888/route1", ds.getInputStream(false))
        Dataset data = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), result)


        then:
        data != null
        data.items.size() == 3
        data.items[0].values.size() == 3
        data.items[0].values.containsKey('route1') // new
        data.items[0].values.containsKey('num')    // original

        cleanup:
        context.stop();
    }


    void "test execute single"() {

        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)


        VariableManager varman = new VariableManager(null, 1, 1);
        Long producer = 1
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                ds)

        HttpServiceDescriptor sd = new HttpServiceDescriptor("id.http", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", 'http://localhost:8888/route1')

        MoleculeServiceThinExecutorStep step = new MoleculeServiceThinExecutorStep()

        step.configure(producer, "job1",
                null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input": new VariableKey(producer, "input")],
                [:],
                sd
        )

        when:
        step.execute(varman, context)
        Dataset result = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

        then:
        result.items.size() == 3
        result.items[0].values.size() == 3
        result.items[0].values.containsKey('route1') // new
        result.items[0].values.containsKey('num')    // original

        cleanup:
        context.stop();
    }

    void "test execute multiple"() {

        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)


        VariableManager varman = new VariableManager(null,1,1);

        Long producer = 1
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                ds)

        HttpServiceDescriptor sd1 = new HttpServiceDescriptor("id.busybox", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", 'http://localhost:8888/route1')

        MoleculeServiceThinExecutorStep step1 = new MoleculeServiceThinExecutorStep()
        step1.configure(producer, "job1",
                null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input": new VariableKey(producer, "input")],
                ["output": "_tmp1"],
                sd1
        )

        HttpServiceDescriptor sd2 = new HttpServiceDescriptor("id.busybox", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", 'http://localhost:8888/route2')

        MoleculeServiceThinExecutorStep step2 = new MoleculeServiceThinExecutorStep()
        step2.configure(producer, "job1",
                null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input": new VariableKey(producer, "_tmp1")],
                ["output": "Output"],
                sd2
        )

        when:
        StepExecutor executor = new StepExecutor(producer, "job1", new ExecuteCellUsingStepsJobDefinition(), varman)
        Step[] steps = [step1, step2] as Step[]
        executor.execute(steps, context)
        Dataset result = varman.getValue(new VariableKey(producer, "Output"), Dataset.class)

        then:
        result.items.size() == 3
        result.items[0].values.size() == 4
        result.items[0].values.containsKey('route1') // new
        result.items[0].values.containsKey('route2') // new
        result.items[0].values.containsKey('num')    // original

        cleanup:
        context.stop();
    }


}