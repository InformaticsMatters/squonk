package org.squonk.execution.steps.impl


import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.io.IORoute
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 * Created by timbo on 06/01/16.
 */
class EchoStepSpec extends Specification {

    void "simple test"() {
        String value = "hello"
        VariableManager varman = new VariableManager(null,1,1);
        Long producer = 1
        varman.putValue(
                new VariableKey(producer, "input"),
                String.class,
                value)

        EchoStep step = new EchoStep()
        step.configure(producer, "job1",
                [:],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input":new VariableKey(producer, "input")],
                ["output":"output"])

        when:
        step.execute(varman, null)
        String result = varman.getValue(new VariableKey(producer, "output"), String.class)

        then:
        result == value
    }
}
