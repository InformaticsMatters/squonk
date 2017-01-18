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
}
