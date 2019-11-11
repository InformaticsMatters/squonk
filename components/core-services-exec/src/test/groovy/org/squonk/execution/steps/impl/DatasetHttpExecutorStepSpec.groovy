/*
 * Copyright (c) 2019 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.execution.steps.impl

import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.core.HttpServiceDescriptor
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.types.MoleculeObject
import org.squonk.types.io.JsonHandler
import org.squonk.util.IOUtils
import spock.lang.Specification

import java.util.stream.Stream

/**
 *
 * @author timbo
 */
class DatasetHttpExecutorStepSpec extends Specification {


    void "test routes simple"() {

        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext()
        context.start()

        Dataset ds = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)
        ProducerTemplate pt = context.createProducerTemplate()

        when:
        InputStream result = pt.requestBody("http4:localhost:8888/route1", ds.getInputStream(false))
        Dataset dataset = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), result)

        then:
        dataset != null
        dataset.getMetadata().getType() == MoleculeObject.class
        dataset.items.size() == 3
        dataset.items[0].values.size() == 3
        dataset.items[0].values.containsKey('route1') // new
        dataset.items[0].values.containsKey('num')    // original

        cleanup:
        context?.stop();
    }


    void "test execute single"() {

        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext()
        context.start()

        Dataset input = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)
        HttpServiceDescriptor sd = new HttpServiceDescriptor("id.http", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", 'http://localhost:8888/route1')

        DatasetHttpExecutorStep step = new DatasetHttpExecutorStep()
        step.configure("test execute single", null, sd, context, null)

        when:
        def resultsMap = step.execute(Collections.singletonMap("input", input))
        def result = resultsMap["output"]

        then:
        result.items.size() == 3
        result.items[0].values.size() == 3
        result.items[0].values.containsKey('route1') // new
        result.items[0].values.containsKey('num')    // original

        cleanup:
        context?.stop();
    }

    void "test thin handling"() {
        Dataset input = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)
        HttpServiceDescriptor sd = new HttpServiceDescriptor("id.http", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", 'http://localhost:8888/route1')

        DatasetHttpExecutorStep step = new DatasetHttpExecutorStep()
        step.configure("test execute single", null, sd, null, null )

        when:
        def results1 = step.prepareInputs([input:input])
        def shouldBeThin = results1["input"]
        shouldBeThin.items
        def results2 = step.prepareOutputs([output:shouldBeThin])
        def shouldBeThick = results2["output"]
        shouldBeThick.items

        then:
        shouldBeThin.items.size() == 3
        shouldBeThin.metadata.type == MoleculeObject.class
        shouldBeThin.items[0].values.size() == 0
        shouldBeThick.metadata.type == MoleculeObject.class
        shouldBeThick.items[0].values.size() == 3
    }

    void "test thin execution"() {
        def routeBuilder = new RouteBuilder() {

            int values = 0

            void configure() {

                restConfiguration().component("jetty").host("0.0.0.0").port(8888);

                rest("/route").post().route().process() { exch ->
                    handle(exch)
                }
            }

            void handle(Exchange exch) {
                InputStream is = exch.in.getBody(InputStream.class)
                Dataset ds = JsonHandler.getInstance().unmarshalDataset(new DatasetMetadata(MoleculeObject.class), IOUtils.getGunzippedInputStream(is))
                Stream s = ds.stream.peek() { values += it.values.size() }
                InputStream out = JsonHandler.getInstance().marshalStreamToJsonArray(s, false)
                exch.in.body = out
            }
        }
        DefaultCamelContext context = ServiceExecutorHelper.createCamelContext(routeBuilder)
        context.start()

        Dataset input = new Dataset(MoleculeObject.class, ServiceExecutorHelper.mols)
        HttpServiceDescriptor sd = new HttpServiceDescriptor("id.http", "name", "desc",  null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", 'http://localhost:8888/route')

        DatasetHttpExecutorStep step = new DatasetHttpExecutorStep()
        step.configure("test thin execution", null, sd, context, null)

        when:
        def resultsMap = step.execute(Collections.singletonMap("input", input))
        def result = resultsMap["output"]

        then:
        result.items.size() == 3
        routeBuilder.values == 0

    }

}