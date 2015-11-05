package com.im.lac.job.jobdef;

/**
 *
 * @author timbo
 */
public interface StepDefinitionConstants {

    public static final String STEP_SDF_READER = "com.im.lac.services.job.service.steps.SDFReaderStep";
    public static final String STEP_CSV_READER = "com.im.lac.services.job.service.steps.CSVReaderStep";
    public static final String STEP_DATASET_WRITER = "com.im.lac.services.job.service.steps.DatasetWriterStep";
    public static final String STEP_DATASET_READER = "com.im.lac.services.job.service.steps.DatasetReaderStep";
    public static final String STEP_MOLECULE_SERVICE_EXECUTOR = "com.im.lac.services.job.service.steps.MoleculeServiceFatExecutorStep";
    public static final String STEP_VALUE_TRANSFORMER = "com.im.lac.services.job.service.steps.ValueTransformerStep";
    public static final String STEP_BASICOBJECT_TO_MOLEUCLEOBJECT = "com.im.lac.services.job.service.steps.BasicObjectToMoleculeObjectStep";
    public static final String STEP_CHEMBL_ACTIVITIES_FETCHER = "com.im.lac.services.job.service.steps.ChemblActivitiesFetcherStep";
    public static final String STEP_DATASET_MERGER = "com.im.lac.services.job.service.steps.DatasetMergerStep";

}
