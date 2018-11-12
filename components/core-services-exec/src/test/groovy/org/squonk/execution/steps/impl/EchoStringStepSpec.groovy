/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

import spock.lang.Specification

/**
 * Created by timbo on 06/01/16.
 */
class EchoStringStepSpec extends Specification {

    void "simple test"() {
        String value = "hello"

        EchoStringStep step = new EchoStringStep()
        step.configure("simple test",
                [:],
                EchoStringStep.SERVICE_DESCRIPTOR)

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", value), null)
        def result = resultsMap["output"]

        then:
        result == value
    }
}
