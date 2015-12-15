package org.squonk.execution.steps;

/**
 *
 * @author timbo
 */
public interface StepDefinitionConstants {

    public static final String STEP_SDF_READER = "org.squonk.execution.steps.impl.SDFReaderStep";
    public static final String STEP_CSV_READER = "org.squonk.execution.steps.impl.CSVReaderStep";
    public static final String STEP_DATASET_WRITER = "org.squonk.execution.steps.impl.DatasetWriterStep";
    public static final String STEP_DATASET_READER = "org.squonk.execution.steps.impl.DatasetReaderStep";
    public static final String STEP_MOLECULE_SERVICE_EXECUTOR = "org.squonk.execution.steps.impl.MoleculeServiceFatExecutorStep";
    public static final String STEP_VALUE_TRANSFORMER = "org.squonk.execution.steps.impl.ValueTransformerStep";
    public static final String STEP_BASICOBJECT_TO_MOLECULEOBJECT = "org.squonk.execution.steps.impl.BasicObjectToMoleculeObjectStep";
    public static final String STEP_CHEMBL_ACTIVITIES_FETCHER = "org.squonk.execution.steps.impl.ChemblActivitiesFetcherStep";
    public static final String STEP_DATASET_MERGER = "org.squonk.execution.steps.impl.DatasetMergerStep";
    
    
    public static final String VARIABLE_INPUT_DATASET = "_InputDataset";
    public static final String VARIABLE_OUTPUT_DATASET = "_OutputDataset";
    public static final String VARIABLE_FILE_INPUT = "FileInput";
    
}
