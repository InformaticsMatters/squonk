package org.squonk.execution.steps.impl

import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.StepExecutor
import org.squonk.execution.variable.PersistenceType
import org.squonk.notebook.api.VariableKey
import com.squonk.dataset.Dataset

import static org.squonk.execution.steps.StepDefinitionConstants.*
import org.squonk.execution.variable.impl.MemoryVariableLoader
import org.squonk.execution.variable.VariableManager


        
MemoryVariableLoader loader = new MemoryVariableLoader()
VariableManager varman = new VariableManager(loader)
        
// cell 1
// step 1: read Cyp2C19 data
StepDefinition step1a = new StepDefinition(
    STEP_CHEMBL_ACTIVITIES_FETCHER,
    [
        (ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL1613777',
        (ChemblActivitiesFetcherStep.OPTION_PREFIX):'Cyp2C19'
    ], [:],
    [(ChemblActivitiesFetcherStep.VAR_OUTPUT_DATASET): 'Cyp2C19_out'])
// end of cell 1

// cell 2
// step 1: read Cyp2C19 data
StepDefinition step2a = new StepDefinition(
    STEP_CHEMBL_ACTIVITIES_FETCHER,
    [
        (ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL1614027',
        (ChemblActivitiesFetcherStep.OPTION_PREFIX):'Cyp2C9'
    ], [:],
    [(ChemblActivitiesFetcherStep.VAR_OUTPUT_DATASET):'Cyp2C9_out'], )
// end of cell 2

// cell 3 - merge
StepDefinition merge1 = new StepDefinition(
    STEP_DATASET_MERGER,
    [(DatasetMergerStep.OPTION_MERGE_FIELD_NAME):'ChemblID'],
        [(DatasetMergerStep.VAR_INPUT_1): new VariableKey('p1', 'Cyp2C19_out'),
         (DatasetMergerStep.VAR_INPUT_2): new VariableKey('p2', 'Cyp2C9_out')],
        [
        (DatasetMergerStep.VAR_OUTPUT):'Merged_out'
        ])
// end of cell 3

println "executing step 1"
StepExecutor exec1 = new StepExecutor('p1', varman)
exec1.execute([step1a] as StepDefinition[], null)

println "executing step 2"
StepExecutor exec2 = new StepExecutor('p2', varman)
exec2.execute([step2a] as StepDefinition[], null)

println "executing merge"
StepExecutor exec3 = new StepExecutor('m', varman)
exec3.execute([merge1] as StepDefinition[], null)


Dataset results = varman.getValue(new VariableKey("m", 'Merged_out'), Dataset.class, PersistenceType.DATASET)
println "Merged results for ${results.metadata.size} records"
println "fields are:"
results.metadata.valueClassMappings.each {
    println "  $it.key $it.value"
}
println results.metadata



