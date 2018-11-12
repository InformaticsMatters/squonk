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
 * Created by timbo on 07/10/16.
 */
class SmilesStructuresStepSpec extends Specification {

    void "read smiles"() {

        String text = "CCCC\nCCCCC\nCCCCCC"
        SmilesStructuresStep step = new SmilesStructuresStep()
        step.configure("read smiles",
                [(SmilesStructuresStep.OPTION_SMILES): text],
                SmilesStructuresStep.SERVICE_DESCRIPTOR)

        when:
        def resultsMap = step.doExecute(null, null)
        def result = resultsMap["output"]

        then:
        result != null
        def items = result.items
        items.size() == 3
        items[0].values.size() == 0

    }

    void "read smiles with names"() {

        String text = "CCCC one\nCCCCC two\nCCCCCC three"
        SmilesStructuresStep step = new SmilesStructuresStep()
        step.configure("read smiles with names",
                [(SmilesStructuresStep.OPTION_SMILES): text],
                SmilesStructuresStep.SERVICE_DESCRIPTOR)

        when:
        def resultsMap = step.execute(null, null)
        def result = resultsMap["output"]

        then:
        result != null
        def items = result.items
        items.size() == 3
        items[0].values.size() == 1
        items[0].values.Name == 'one'

    }
}
