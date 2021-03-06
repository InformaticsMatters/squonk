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

import org.squonk.dataset.Dataset
import org.squonk.types.BasicObject
import spock.lang.Specification

/**
 * Created by timbo on 16/05/17.
 */
class DatasetUUIDFilterStepSpec extends Specification {


    def input = [
            new BasicObject([i: 1, f: 1.1f, s: 'one']),
            new BasicObject([i: 2, f: 2.2f, s: 'two']),
            new BasicObject([i: 3, f: 3.3f, s: 'three']),
            new BasicObject([i: 4, f: 4.4f, s: 'four']),
            new BasicObject([i: 5, f: 5.5f, s: 'five']),
            new BasicObject([i: 6, f: 6.6f, s: 'six']),
    ]

    void "simple filter step"() {

        def uuids = input[1].UUID.toString() + "," + input[3].UUID.toString() + " , \n" + input[5].UUID.toString()


        DatasetUUIDFilterStep step = new DatasetUUIDFilterStep()
        step.configure("simple filter step",
                [(DatasetUUIDFilterStep.OPTION_UUIDS): uuids],
                DatasetUUIDFilterStep.SERVICE_DESCRIPTOR,
                null, null
        )
        Dataset ds = new Dataset(BasicObject.class, input)
        ds.generateMetadata()

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", ds))
        def output = resultsMap["output"]

        then:

        output != null
        output.items.size() == 3
        output.items.find { it.UUID == input[1].UUID} != null
        output.items.find { it.UUID == input[3].UUID} != null
        output.items.find { it.UUID == input[5].UUID} != null
    }

    void "test parse"() {

        when:
        DatasetUUIDFilterStep step = new DatasetUUIDFilterStep()

        then:
        step.parseUUIDs("""${UUID.randomUUID().toString()} ${UUID.randomUUID().toString()} , ${UUID.randomUUID().toString()},${UUID.randomUUID().toString()}
 ${UUID.randomUUID().toString()}\n\n${UUID.randomUUID().toString()} \n${UUID.randomUUID().toString()}, ${UUID.randomUUID().toString()}""").size() == 8
    }


}
