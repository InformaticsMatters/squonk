package org.squonk.execution.steps.impl


import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableClient
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 * Created by timbo on 06/01/16.
 */
class EchoStepSpec extends Specification {

    void "simple test"() {
        String value = "hello"
        VariableManager varman = new VariableManager(new MemoryVariableClient(),1,1);
        Long producer = 1
        varman.putValue(
                new VariableKey(producer, "input"),
                String.class,
                value)

        EchoStep step = new EchoStep()
        step.configure(producer,
                [:],
                ["input":new VariableKey(producer, "input")],
                ["output":"output"])

        when:
        step.execute(varman, null)
        String result = varman.getValue(new VariableKey(producer, "output"), String.class)

        then:
        result == value
    }
}
