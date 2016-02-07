package org.squonk.execution.steps

import org.squonk.notebook.api.VariableKey
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 01/02/16.
 */
class StepDefintionSpec extends Specification {

    void "test to/from json"() {

        StepDefinition step1 = new StepDefinition('foo.bar.Baz')
                .withOptions(['hello':'world'])
                .withInputVariableMappings(["in":new VariableKey('p','input')])
                .withOutputVariableMappings([out:'output'])

        when:
        String json = JsonHandler.getInstance().objectToJson(step1)
        StepDefinition step2 = JsonHandler.getInstance().objectFromJson(json, StepDefinition.class)

        then:
        step2.getOptions().size() == 1
        step2.getInputVariableMappings().size() == 1
        step2.getOutputVariableMappings().size() == 1
        step2.getImplementationClass() == 'foo.bar.Baz'
    }

}
