/*
 * Copyright (c) 2020 Informatics Matters Ltd.
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

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 13/09/16.
 */
class DatasetSplitUsingExpressionStepSpec extends Specification {

    def createDataset() {
        def mols = [
                new BasicObject([idx: 0, a: 11, b: 'red', c: 7, d: 5]),
                new BasicObject([idx: 1, a: 23, b: 'blue', c: 5]),
                new BasicObject([idx: 2, a: 7, b: 'green', c: 5, d: 7]),
                new BasicObject([idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        ds.generateMetadata()
        return ds
    }

    def createStep(field, expression, jobId, camelContext) {
        DatasetSplitUsingExpressionStep step = new DatasetSplitUsingExpressionStep()
        step.configure(jobId,
                [(DatasetSplitUsingExpressionStep.OPTION_FIELD): field, (DatasetSplitUsingExpressionStep.OPTION_EXPR): expression],
                DatasetSplitUsingExpressionStep.SERVICE_DESCRIPTOR,
                camelContext,
                null
        )
        return step
    }

    void "split lt tests"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitUsingExpressionStep step = createStep("a", "< 10", "splitonexpr tests", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 1
        fail.items.size() == 3
    }

    void "split eq tests"() {

        when:
        DefaultCamelContext context = new DefaultCamelContext()
        DatasetSplitUsingExpressionStep step = createStep("c", "=5", "splitonexpr tests", context)
        Dataset input = createDataset()

        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def pass = resultsMap["pass"]
        def fail = resultsMap["fail"]

        then:
        pass.items.size() == 2
        fail.items.size() == 2
    }


    void "expression tests"() {

        DatasetSplitUsingExpressionStep step = new DatasetSplitUsingExpressionStep()

        expect:
        step.createPredicate(expression, type).test(value) == result

        where:
        expression | value | type          | result
        '< 10'     | 7     | Integer.class | true
        '< 10'     | 10    | Integer.class | false
        '< 10'     | 11    | Integer.class | false
        '<= 10'    | 7     | Integer.class | true
        '<= 10'    | 10    | Integer.class | true
        '<= 10'    | 11    | Integer.class | false
        '< 10'     | 7f    | Float.class   | true
        '< 10'     | 10f   | Float.class   | false
        '< 10'     | 11f   | Float.class   | false
        '<= 10'    | 7f    | Float.class   | true
        '<= 10'    | 10f   | Float.class   | true
        '<= 10'    | 11f   | Float.class   | false
        '> 10'     | 7     | Integer.class | false
        '> 10'     | 10    | Integer.class | false
        '> 10'     | 11    | Integer.class | true
        '= 10'     | 7     | Integer.class | false
        '= 10'     | 10    | Integer.class | true
        '=10'      | 10    | Integer.class | true
        '< 10'     | 7d    | Double.class  | true
        '= red'    | 'red' | String.class  | true
        '= red'    | 'abc' | String.class  | false
        '< abc'    | 'xyz' | String.class  | false
        '> abc'    | 'xyz' | String.class  | true
        '< 10'     | null  | Integer.class | false
    }
}