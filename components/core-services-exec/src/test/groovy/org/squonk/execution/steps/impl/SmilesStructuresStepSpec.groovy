package org.squonk.execution.steps.impl

import org.squonk.dataset.Dataset
import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 * Created by timbo on 07/10/16.
 */
class SmilesStructuresStepSpec extends Specification {


    Long producer = 1

    void "read smiles"() {

        String text = "CCCC\nCCCCC\nCCCCCC"
        VariableManager varman = new VariableManager(null, 1, 1);
        SmilesStructuresStep step = new SmilesStructuresStep()

        step.configure(producer, "job1",
                [(SmilesStructuresStep.OPTION_SMILES): text],
                [:], [:])

        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, SmilesStructuresStep.VAR_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 0

    }

    void "read smiles with names"() {

        String text = "CCCC one\nCCCCC two\nCCCCCC three"
        VariableManager varman = new VariableManager(null, 1, 1);
        SmilesStructuresStep step = new SmilesStructuresStep()

        step.configure(producer, "job1",
                [(SmilesStructuresStep.OPTION_SMILES): text],
                [:], [:])

        when:
        step.execute(varman, null)
        Dataset dataset = varman.getValue(new VariableKey(producer, SmilesStructuresStep.VAR_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 1
        items[0].values.Name == 'one'

    }
}
