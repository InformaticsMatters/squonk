package org.squonk.execution.steps.impl

import org.squonk.dataset.Dataset
import org.squonk.execution.variable.PersistenceType
import org.squonk.execution.variable.VariableManager
import org.squonk.execution.variable.impl.MemoryVariableLoader
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ChemblActivitiesFetcherStepSpec extends Specification {
	
    void "test fetch"() {
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
    
        
        ChemblActivitiesFetcherStep step = new ChemblActivitiesFetcherStep()
        String producer = "p"
        step.configure(producer,
                [(ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL864878'],
                [:], [:])
        
        when:
        step.execute(varman, null)
        
        then:

        def dataset = varman.getValue(new VariableKey(producer, ChemblActivitiesFetcherStep.VAR_OUTPUT_DATASET), Dataset.class, PersistenceType.DATASET)
        dataset != null
        dataset.items.size() == 10
        
    }
}

