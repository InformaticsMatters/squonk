package org.squonk.execution.steps.impl

import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
import com.squonk.dataset.DatasetMetadata
import org.squonk.execution.steps.Step
import org.squonk.execution.variable.PersistenceType
import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableLoader
import com.squonk.types.io.JsonHandler
import com.squonk.util.IOUtils
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.execution.steps.StepExecutor
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

import java.util.stream.Stream

/**
 *
 * @author timbo
 */
class MoleculeServiceThinExecutorStepSpec extends Specification {

    def mols = [
            new MoleculeObject("C", "smiles", [num: "1", hello: 'world']),
            new MoleculeObject("CC", "smiles", [num: "99", hello: 'mars']),
            new MoleculeObject("CCC", "smiles", [num: "100", hello: 'mum'])
    ]

    CamelContext createCamelContext() {
        DefaultCamelContext context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {
            void configure() {

                restConfiguration().component("jetty").host("0.0.0.0").port(8888);

                rest("/route1").post().route().process() { exch ->
                    handle(exch, "route1", 99)
                }

                rest("/route2").post().route().process() { exch ->
                    handle(exch, "route2", 88)
                }
            }

            void handle(Exchange exch, String prop, Object value) {
                InputStream is = exch.in.getBody(InputStream.class)
                Dataset ds = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), IOUtils.getGunzippedInputStream(is))
                Stream s = ds.stream.peek() { it.putValue(prop, value) }
                InputStream out = JsonHandler.getInstance().marshalStreamToJsonArray(s, false)
                exch.in.body = out
            }
        })

        return context
    }

    void "test routes simple"() {

        DefaultCamelContext context = createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, mols)

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

        DefaultCamelContext context = createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, mols)


        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        String producer = "p"
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                ds,
                PersistenceType.NONE)

        MoleculeServiceThinExecutorStep step = new MoleculeServiceThinExecutorStep()

        step.configure(producer,
                [(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT): 'http://localhost:8888/route1'],
                [(MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [:])

        when:
        step.execute(varman, context)
        Dataset result = varman.getValue(new VariableKey(producer, MoleculeServiceThinExecutorStep.VAR_OUTPUT_DATASET), Dataset.class, PersistenceType.DATASET)

        then:
        result.items.size() == 3
        result.items[0].values.size() == 3
        result.items[0].values.containsKey('route1') // new
        result.items[0].values.containsKey('num')    // original

        cleanup:
        context.stop();
    }

    void "test execute multiple"() {

        DefaultCamelContext context = createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, mols)


        VariableManager varman = new VariableManager(new MemoryVariableLoader());

        String producer = "p"
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                ds,
                PersistenceType.NONE)

        MoleculeServiceThinExecutorStep step1 = new MoleculeServiceThinExecutorStep()
        step1.configure(producer,
                [(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT): 'http://localhost:8888/route1'],
                [(MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET): new VariableKey(producer, "input")],
                [(MoleculeServiceThinExecutorStep.VAR_OUTPUT_DATASET): "_tmp1"])

        MoleculeServiceThinExecutorStep step2 = new MoleculeServiceThinExecutorStep()
        step2.configure(producer,
                [(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT): 'http://localhost:8888/route2'],
                [(MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET): new VariableKey(producer, "_tmp1")],
                [(MoleculeServiceThinExecutorStep.VAR_OUTPUT_DATASET): "Output"])

        when:
        StepExecutor executor = new StepExecutor(producer, varman)
        Step[] steps = [step1, step2] as Step[]
        executor.execute(steps, context)
        Dataset result = varman.getValue(new VariableKey(producer, "Output"), Dataset.class, PersistenceType.DATASET)

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