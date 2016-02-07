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


    public static final String STEP_TRUSTED_GROOVY_DATASET_SCRIPT = "org.squonk.execution.steps.impl.TrustedGroovyDatasetScriptStep";
    
    
    public static final String VARIABLE_INPUT_DATASET = "_InputDataset";
    public static final String VARIABLE_OUTPUT_DATASET = "_OutputDataset";
    public static final String VARIABLE_FILE_INPUT = "_FileInput";

    interface ChemblActivitiesFetcher {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.ChemblActivitiesFetcherStep";
        public static final String OPTION_ASSAY_ID = "assayId";
        public static final String OPTION_PREFIX = "prefix";
        public static final String OPTION_BATCH_SIZE = "batchSize";
    }

    interface DatasetMerger {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.DatasetMergerStep";
        public static final String OPTION_MERGE_FIELD_NAME = "mergeFieldName";
        public static final String OPTION_KEEP_FIRST = "keepFirst";
    }

    interface SdfUpload {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.SDFReaderStep";
        public static final String OPTION_NAME_FIELD_NAME = "nameFieldName";
    }

    interface CsvUpload {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.CSVReaderStep";
        public static final String OPTION_CSV_FORMAT_TYPE = "csvFormatType";
        public static final String OPTION_NAME_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    }
    
}
