package com.squonk.notebook.execution.steps

import com.squonk.notebook.execution.variable.*
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
        varman.getVariables().size() == 1
        def dataset = varman.getValue(varman.getVariables()[0])
        dataset != null
        dataset.items.size() == 10
        
    }
}

