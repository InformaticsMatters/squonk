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
package org.squonk.rdkit.mol

import org.RDKit.RWMol
import spock.lang.Specification

class MolEvaluatorSpec extends Specification {

    static def evaluator = new MolEvaluator()
    static def mol = RWMol.MolFromSmiles("NC1C=CC=CC1")

    void "test calcs"() {

        when:
        def calcs = EvaluatorDefinition.Function.values()

        then:
        calcs.each {
            def val = evaluator.calculate(mol, it)
            println "Testing $it $val"
            assert val != null : "$it failed"
        }

    }

}