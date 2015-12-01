package com.squonk.execution.steps.impl

import com.squonk.dataset.Dataset
import com.squonk.execution.variable.*
import com.squonk.execution.variable.impl.*
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ChemblActivitiesFetcherStepSpec extends Specification {
	
    void "test fetch"() {
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
    
        
        ChemblActivitiesFetcherStep step = new ChemblActivitiesFetcherStep()
        step.configure([(ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL864878'], [:])
        
        when:
        step.execute(varman, null)
        
        then:

        def dataset = varman.getValue(ChemblActivitiesFetcherStep.VAR_OUTPUT_DATASET, Dataset.class, Variable.PersistenceType.DATASET)
        dataset != null
        dataset.items.size() == 10
        
    }
}

