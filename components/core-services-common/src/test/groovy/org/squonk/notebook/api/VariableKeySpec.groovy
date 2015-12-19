package org.squonk.notebook.api

import spock.lang.Specification

/**
 * Created by timbo on 17/12/15.
 */
class VariableKeySpec extends Specification {

    void "test equals"() {

        when:
        VariableKey a = new VariableKey("p", "n")
        VariableKey b = new VariableKey("p", "n")
        VariableKey c = new VariableKey("", "n")
        VariableKey d = new VariableKey("", "n")

        then:
        a.equals(b)
        c.equals(d)
    }
}
