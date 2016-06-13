package org.squonk.execution.steps.impl

import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ChemblActivitiesFetcherStepSpec extends Specification {
	
    void "test fetch"() {
        VariableManager varman = new VariableManager(null,1,1);
    
        
        ChemblActivitiesFetcherStep step = new ChemblActivitiesFetcherStep()
        Long producer = 1
        step.configure(producer, "job1",
                [(ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL864878'],
                [:], [:])
        
        when:
        step.execute(varman, null)
        
        then:

        def dataset = varman.getValue(new VariableKey(producer, ChemblActivitiesFetcherStep.VAR_OUTPUT_DATASET), Dataset.class)
        dataset != null
        dataset.items.size() == 10
        
    }
}

