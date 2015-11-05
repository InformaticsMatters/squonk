package com.im.lac.services.job.service.steps

import static com.im.lac.job.jobdef.StepDefinitionConstants.*
import com.im.lac.job.jobdef.StepDefinition
import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import org.apache.camel.builder.RouteBuilder

import org.apache.camel.impl.DefaultCamelContext

        
MemoryVariableLoader loader = new MemoryVariableLoader()
VariableManager varman = new VariableManager(loader)
StepExecutor exec = new StepExecutor(varman);
        
        
// cell 1
// step 1: read Cyp2C19 data
StepDefinition step1a = new StepDefinition(
    STEP_CHEMBL_ACTIVITIES_FETCHER,
    [
        (ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL1613777',
        (ChemblActivitiesFetcherStep.OPTION_PREFIX):'Cyp2C19'
    ],
    [(ChemblActivitiesFetcherStep.FIELD_OUTPUT_DATASET):'_Cyp2C19_out'], )

StepDefinition step1b = new StepDefinition(
    STEP_DATASET_WRITER,
    [:],
    [(DatasetWriterStep.FIELD_INPUT_DATASET):'_Cyp2C19_out', 
        (DatasetWriterStep.FIELD_OUTPUT_DATASET):'Cyp2C19_out'])
// end of cell 1

// cell 2
// step 1: read Cyp2C19 data
StepDefinition step2a = new StepDefinition(
    STEP_CHEMBL_ACTIVITIES_FETCHER,
    [
        (ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL1614027',
        (ChemblActivitiesFetcherStep.OPTION_PREFIX):'Cyp2C9'
    ],
    [(ChemblActivitiesFetcherStep.FIELD_OUTPUT_DATASET):'_Cyp2C9_out'], )

StepDefinition step2b = new StepDefinition(
    STEP_DATASET_WRITER,
    [:],
    [
        (DatasetWriterStep.FIELD_INPUT_DATASET):'_Cyp2C9_out', 
        (DatasetWriterStep.FIELD_OUTPUT_DATASET):'Cyp2C9_out'])
// end of cell 2

// cell 3
StepDefinition merge1 = new StepDefinition(
    STEP_DATASET_MERGER,
    [(DatasetMergerStep.OPTION_MERGE_FIELD_NAME):'ChemblID'],
    [
        (DatasetMergerStep.FIELD_INPUT_1):'Cyp2C19_out', 
        (DatasetMergerStep.FIELD_INPUT_2):'Cyp2C9_out',
        (DatasetMergerStep.FIELD_OUTPUT):'Merged_out'
    ])
// end of cell 3

println "executing steps 1"
exec.execute([step1a, step1b] as StepDefinition[], null)
reportVars(varman)
println "executing steps 2"
exec.execute([step2a, step2b] as StepDefinition[], null)
reportVars(varman)
println "executing merge"
exec.execute([merge1] as StepDefinition[], null)
reportVars(varman)

void reportVars(varman) {
    println "${varman.variables.size()} variables"
    varman.variables.each {
        println "  ${it.name}"
    }
}






