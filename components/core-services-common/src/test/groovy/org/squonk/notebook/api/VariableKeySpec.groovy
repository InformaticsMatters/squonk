package org.squonk.notebook.api

import spock.lang.Specification

/**
 * Created by timbo on 17/12/15.
 */
class VariableKeySpec extends Specification {

    void "test equals"() {

        when:
        VariableKey a = new VariableKey(1, "n")
        VariableKey b = new VariableKey(1, "n")
        VariableKey c = new VariableKey(2, "n")
        VariableKey d = new VariableKey(2, "n")

        then:
        a.equals(b)
        c.equals(d)
    }
}
