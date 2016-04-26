package org.squonk.execution.steps;

/**
 *
 * @author timbo
 */
public interface StepDefinitionConstants {

    public static final String STEP_DATASET_WRITER = "org.squonk.execution.steps.impl.DatasetWriterStep";
    public static final String STEP_DATASET_READER = "org.squonk.execution.steps.impl.DatasetReaderStep";

    public static final String VARIABLE_INPUT_DATASET = "_InputDataset";
    public static final String VARIABLE_OUTPUT_DATASET = "_OutputDataset";
    public static final String VARIABLE_FILE_INPUT = "_FileInput";

    interface ServiceExecutor {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.MoleculeServiceThinExecutorStep";
        public static final String OPTION_SERVICE_PARAMS = "serviceParams"; // Map of params for the service
        public static final String OPTION_SERVICE_ENDPOINT = "serviceEndpoint"; // the URL for the service
        public static final String OPTION_PRESERVE_STRUCTURE = "option.preserveStructure"; //Boolean
        public static final String OPTION_FILTER = "option.filter"; //Boolean
    }

    interface OutOnlyMoleculeServiceExecutor {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.OutOnlyMoleculeServiceExecutorStep";
        public static final String OPTION_SERVICE_PARAMS = "serviceParams"; // Map of params for the service
        public static final String OPTION_SERVICE_ENDPOINT = "serviceEndpoint"; // the URL for the service
        public static final String OPTION_PRESERVE_STRUCTURE = "option.preserveStructure"; //Boolean
        public static final String OPTION_FILTER = "option.filter"; //Boolean
    }

    interface ChemblActivitiesFetcher {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.ChemblActivitiesFetcherStep";
        public static final String OPTION_ASSAY_ID = "assayId";
        public static final String OPTION_PREFIX = "prefix";
        public static final String OPTION_BATCH_SIZE = "batchSize";
    }

    interface DatasetMerger {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.DatasetMergerStep";

        /**
         * The name of the value to use to merge the entries. If undefined then the
         * UUID is used which is probably not what you want. Expects a String value.
         */
        public static final String OPTION_MERGE_FIELD_NAME = "mergeFieldName";

        /**
         * In the case of duplicate field names whether to keep the original value
         * (true) or to replace this with newly found value (false). Expects a
         * Boolean value. Default is true.
         */
        public static final String OPTION_KEEP_FIRST = "keepFirst";
    }

    interface SdfUpload {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.SDFReaderStep";
        public static final String OPTION_FILE_UPLOAD = "fileUpload";
        public static final String OPTION_NAME_FIELD_NAME = "nameFieldName";
    }

    interface CsvUpload {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.CSVReaderStep";
        public static final String OPTION_FILE_UPLOAD = "fileUpload";
        public static final String OPTION_CSV_FORMAT_TYPE = "csvFormatType";
        public static final String OPTION_NAME_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    }

    interface ConvertBasicToMoleculeObject {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.BasicObjectToMoleculeObjectStep";
        /** The name of the value in the BasicObject that contains the structure
         */
        public static final String OPTION_STRUCTURE_FIELD_NAME = "structureFieldName";

        /** Optional value for the structure format. e.g. "smiles". */
        public static final String OPTION_STRUCTURE_FORMAT = "structureFormat";

        /** Option for whether to use the UUID of BasicObject as the UUID of the new MoleculeObject.
         * Default is true. Expects a boolean value.
         */
        public static final String OPTION_PRESERVE_UUID = "preserveUuid";
    }

    interface TrustedGroovyDataset {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.TrustedGroovyDatasetScriptStep";
        public static final String OPTION_SCRIPT = "script";
    }

    interface UnrustedGroovyDataset {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.UntrustedGroovyDatasetScriptStep";
        public static final String OPTION_SCRIPT = "script";
    }

    interface ValueTransformer {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.ValueTransformerStep";
        public static final String OPTION_TRANSFORMS = "transformDefinitions";
    }

    interface DockerProcessDataset {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.DockerProcessDatasetStep";
        public static final String OPTION_DOCKER_IMAGE = "dockerImage";
        public static final String OPTION_DOCKER_COMMAND  = "dockerCommand";
    }

    interface DatasetFilterGroovy {
        public static final String CLASSNAME = "org.squonk.execution.steps.impl.DatasetFilterGroovyStep";
        public static final String OPTION_SCRIPT = "script";
    }
    
}
