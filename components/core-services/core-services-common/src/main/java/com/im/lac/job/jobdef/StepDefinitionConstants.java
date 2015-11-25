package com.im.lac.job.jobdef;

/**
 *
 * @author timbo
 */
public interface StepDefinitionConstants {

    public static final String STEP_SDF_READER = "com.squonk.notebook.execution.steps.SDFReaderStep";
    public static final String STEP_CSV_READER = "com.squonk.notebook.execution.steps.CSVReaderStep";
    public static final String STEP_DATASET_WRITER = "com.squonk.notebook.execution.steps.DatasetWriterStep";
    public static final String STEP_DATASET_READER = "com.squonk.notebook.execution.steps.DatasetReaderStep";
    public static final String STEP_MOLECULE_SERVICE_EXECUTOR = "com.squonk.notebook.execution.steps.MoleculeServiceFatExecutorStep";
    public static final String STEP_VALUE_TRANSFORMER = "com.squonk.notebook.execution.steps.ValueTransformerStep";
    public static final String STEP_BASICOBJECT_TO_MOLEUCLEOBJECT = "com.squonk.notebook.execution.steps.BasicObjectToMoleculeObjectStep";
    public static final String STEP_CHEMBL_ACTIVITIES_FETCHER = "com.squonk.notebook.execution.steps.ChemblActivitiesFetcherStep";
    public static final String STEP_DATASET_MERGER = "com.squonk.notebook.execution.steps.DatasetMergerStep";
    
    
    public static final String VARIABLE_INPUT_DATASET = "_InputDataset";
    public static final String VARIABLE_OUTPUT_DATASET = "_OutputDataset";
    public static final String VARIABLE_FILE_INPUT = "FileInput";
    
}
