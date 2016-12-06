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
}
