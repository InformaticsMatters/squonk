package org.squonk.execution.steps.impl

import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeServiceToBasicObjectThinExecutorStepSpec extends Specification {


    void "test routes simple"() {

        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)

        ProducerTemplate pt = context.createProducerTemplate()


        when:
        InputStream result = pt.requestBody("http4:localhost:8888/route3", ds.getInputStream(false))
        Dataset data = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(BasicObject.class), result)


        then:
        data != null
        data.items.size() == 3
        data.items[0] instanceof BasicObject
        data.items[0].values.containsKey('route3') // new

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

        MoleculeServiceToBasicObjectThinExecutorStep step = new MoleculeServiceToBasicObjectThinExecutorStep()

        step.configure(producer, "job1",
                [(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT): 'http://localhost:8888/route3'],
                [(MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [:])

        when:
        step.execute(varman, context)
        Dataset result = varman.getValue(new VariableKey(producer, MoleculeServiceToBasicObjectThinExecutorStep.VAR_OUTPUT_DATASET), Dataset.class)

        then:
        result.items.size() == 3
        result.items[0] instanceof BasicObject
        result.items[0].values.containsKey('route3') // new

        cleanup:
        context.stop();
    }

}