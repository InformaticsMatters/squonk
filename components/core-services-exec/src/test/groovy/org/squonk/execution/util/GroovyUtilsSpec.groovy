/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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

package org.squonk.execution.util

import spock.lang.Specification

/**
 * Created by timbo on 04/12/16.
 */
class GroovyUtilsSpec extends Specification {

    void "simple template"() {

        def t = 'hello ${who}!'
        def m = [who: 'world']

        when:
        def r = GroovyUtils.expandTemplate(t,m)

        then:
        r == 'hello world!'

    }

    void "complex template"() {

        def t = 'screen.py \'${query}\' ${threshold} --d ${descriptor}'
        def m = [query: 'smiles', threshold:0.7, descriptor:'morgan2']

        when:
        def r = GroovyUtils.expandTemplate(t,m)

        then:
        r == "screen.py 'smiles' 0.7 --d morgan2"

    }


    void "optional argument template"() {

        def t = '''screen.py '${query}'${binding.variables.containsKey("threshold") ? ' -t ' + binding.variables.get("threshold") : ''} -d ${descriptor}'''
        def m1 = [query: 'smiles', descriptor:'morgan2']
        def m2 = [query: 'smiles', threshold:0.7, descriptor:'morgan2']

        when:
        def r1 = GroovyUtils.expandTemplate(t,m1)
        def r2 = GroovyUtils.expandTemplate(t,m2)

        then:
        r1 == "screen.py 'smiles' -d morgan2"
        r2 == "screen.py 'smiles' -t 0.7 -d morgan2"

    }

    void "expand values"() {

        def templates = [val1: '$val1', val2: '$val2']
        def values = [val1: "aValue"]

        when:
        def results = GroovyUtils.expandValues(templates, values)

        then:
        results.val1 == "aValue"
        results.val2 == null
    }
}
