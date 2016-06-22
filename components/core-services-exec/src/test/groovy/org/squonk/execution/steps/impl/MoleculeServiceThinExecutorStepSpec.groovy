package org.squonk.execution.steps.impl

import org.squonk.types.MoleculeObject
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.steps.Step
import org.squonk.execution.steps.StepExecutor

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
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

        MoleculeServiceThinExecutorStep step = new MoleculeServiceThinExecutorStep()

        step.configure(producer, "job1",
                [(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT): 'http://localhost:8888/route1'],
                [(MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [:])

        when:
        step.execute(varman, context)
        Dataset result = varman.getValue(new VariableKey(producer, MoleculeServiceThinExecutorStep.VAR_OUTPUT_DATASET), Dataset.class)

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

        MoleculeServiceThinExecutorStep step1 = new MoleculeServiceThinExecutorStep()
        step1.configure(producer, "job1",
                [(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT): 'http://localhost:8888/route1'],
                [(MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [(MoleculeServiceThinExecutorStep.VAR_OUTPUT_DATASET): "_tmp1"])

        MoleculeServiceThinExecutorStep step2 = new MoleculeServiceThinExecutorStep()
        step2.configure(producer, "job1",
                [(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT): 'http://localhost:8888/route2'],
                [(MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET): new VariableKey(producer, "_tmp1")],
                [(MoleculeServiceThinExecutorStep.VAR_OUTPUT_DATASET): "Output"])

        when:
        StepExecutor executor = new StepExecutor(producer, "job1", varman)
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