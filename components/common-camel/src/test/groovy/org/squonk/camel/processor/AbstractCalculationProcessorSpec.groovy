package org.squonk.camel.processor

import org.squonk.types.MoleculeObject
import spock.lang.Specification

class AbstractCalculationProcessorSpec extends Specification {

    void "a + b"() {

        def m = new MoleculeObject("smiles", "smiles")
        m.putValue("a", 4d)
        m.putValue("b", 1d)


        def proc = new AbstractCalculationProcessor("a_plus_b",
                "a_plus_b",
                Double.class,
                null,
                null) {

            protected void processMoleculeObject(MoleculeObject mo) {
                double a = mo.getValue("a", Double.class)
                double b = mo.getValue("b", Double.class)
                mo.putValue(calculatedPropertyName, a + b)
            }
        }

        when:
        proc.processMoleculeObject(m)

        then:
        m.getValue("a_plus_b", Double) == 5d

    }
}
