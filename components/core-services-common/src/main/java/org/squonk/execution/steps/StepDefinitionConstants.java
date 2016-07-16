package org.squonk.execution.steps;

/**
 *
 * @author timbo
 */
public interface StepDefinitionConstants {

    String STEP_DATASET_WRITER = "org.squonk.execution.steps.impl.DatasetWriterStep";
    String STEP_DATASET_READER = "org.squonk.execution.steps.impl.DatasetReaderStep";

    String VARIABLE_INPUT_DATASET = "_InputDataset";
    String VARIABLE_OUTPUT_DATASET = "_OutputDataset";
    String VARIABLE_FILE_INPUT = "_FileInput";

    String OPTION_BODY = "body";
    String OPTION_BODY_CONTENT_TYPE = "body-content-type";
    String OPTION_SERVICE_ENDPOINT = "serviceEndpoint"; // the URL for the service
    String OPTION_MEDIA_TYPE_INPUT = "inputMediaType"; // the format for the input
    String OPTION_MEDIA_TYPE_OUTPUT = "outputMediaType"; // the format for the output
    String OPTION_DOCKER_IMAGE = "dockerImage";

    interface MoleculeServiceThinExecutor {
        String CLASSNAME = "org.squonk.execution.steps.impl.MoleculeServiceThinExecutorStep";
        String OPTION_PRESERVE_STRUCTURE = "option.preserveStructure"; //Boolean
        String OPTION_FILTER = "option.filter"; //Boolean
    }

    interface MoleculeServiceBasicExecutor {
        String CLASSNAME = "org.squonk.execution.steps.impl.MoleculeServiceBasicExecutorStep";
    }

    interface DatasetServiceExecutor {
        String CLASSNAME = "org.squonk.execution.steps.impl.DatasetServiceExecutorStep";
    }

    interface OutOnlyMoleculeServiceExecutor {
        String CLASSNAME = "org.squonk.execution.steps.impl.OutOnlyMoleculeServiceExecutorStep";
    }

    interface ChemblActivitiesFetcher {
        String CLASSNAME = "org.squonk.execution.steps.impl.ChemblActivitiesFetcherStep";
        String OPTION_ASSAY_ID = "assayId";
        String OPTION_PREFIX = "prefix";
        String OPTION_BATCH_SIZE = "batchSize";
    }

    interface DatasetMerger {
        String CLASSNAME = "org.squonk.execution.steps.impl.DatasetMergerStep";

        /**
         * The name of the value to use to merge the entries. If undefined then the
         * UUID is used which is probably not what you want. Expects a String value.
         */
        String OPTION_MERGE_FIELD_NAME = "mergeFieldName";

        /**
         * In the case of duplicate field names whether to keep the original value
         * (true) or to replace this with newly found value (false). Expects a
         * Boolean value. Default is true.
         */
        String OPTION_KEEP_FIRST = "keepFirst";
    }

    interface SdfUpload {
        String CLASSNAME = "org.squonk.execution.steps.impl.SDFReaderStep";
        String OPTION_FILE_UPLOAD = "fileUpload";
        String OPTION_NAME_FIELD_NAME = "nameFieldName";
    }

    interface CsvUpload {
        String CLASSNAME = "org.squonk.execution.steps.impl.CSVReaderStep";
        String OPTION_FILE_UPLOAD = "fileUpload";
        String OPTION_CSV_FORMAT_TYPE = "csvFormatType";
         String OPTION_NAME_FIRST_LINE_IS_HEADER = "firstLineIsHeader";
    }

    interface ConvertBasicToMoleculeObject {
         String CLASSNAME = "org.squonk.execution.steps.impl.BasicObjectToMoleculeObjectStep";
        /** The name of the value in the BasicObject that contains the structure
         */
         String OPTION_STRUCTURE_FIELD_NAME = "structureFieldName";

        /** Optional value for the structure format. e.g. "smiles". */
         String OPTION_STRUCTURE_FORMAT = "structureFormat";

        /** Option for whether to use the UUID of BasicObject as the UUID of the new MoleculeObject.
         * Default is true. Expects a boolean value.
         */
         String OPTION_PRESERVE_UUID = "preserveUuid";
    }

    interface TrustedGroovyDataset {
         String CLASSNAME = "org.squonk.execution.steps.impl.TrustedGroovyDatasetScriptStep";
         String OPTION_SCRIPT = "script";
    }

    interface UntrustedGroovyDatasetScript {
         String CLASSNAME = "org.squonk.execution.steps.impl.UntrustedGroovyDatasetScriptStep";
         String OPTION_SCRIPT = "script";
    }

    interface UntrustedPythonDatasetScript {
        String CLASSNAME = "org.squonk.execution.steps.impl.UntrustedPythonDatasetScriptStep";
        String OPTION_SCRIPT = "script";
    }

    interface UntrustedRDatasetScript {
        String CLASSNAME = "org.squonk.execution.steps.impl.UntrustedRDatasetScriptStep";
        String OPTION_SCRIPT = "script";
    }

    interface ValueTransformer {
         String CLASSNAME = "org.squonk.execution.steps.impl.ValueTransformerStep";
         String OPTION_TRANSFORMS = "transformDefinitions";
    }

    interface DockerProcessDataset {
         String CLASSNAME = "org.squonk.execution.steps.impl.DockerProcessDatasetStep";
         String OPTION_DOCKER_COMMAND  = "dockerCommand";
    }

    interface DatasetFilterGroovy {
         String CLASSNAME = "org.squonk.execution.steps.impl.DatasetFilterGroovyStep";
         String OPTION_SCRIPT = "script";
    }

    interface SmilesDeduplicator {
         String CLASSNAME = "org.squonk.execution.steps.impl.SmilesDeduplicatorStep";
         String OPTION_CANONICAL_SMILES_FIELD = "canonicalSmilesField";
         String OPTION_KEEP_FIRST_FIELDS = "keepFirstFields";
         String OPTION_KEEP_LAST_FIELDS = "keepLastFields";
         String OPTION_APPEND_FIELDS = "appendFields";
    }

    interface DatasetSelectSlice {
         String CLASSNAME = "org.squonk.execution.steps.impl.DatasetSelectSliceStep";
         String OPTION_SKIP = "skip";
         String OPTION_COUNT = "count";
    }

    interface DatasetSelectRandom {
         String CLASSNAME = "org.squonk.execution.steps.impl.DatasetSelectRandomStep";
         String OPTION_RANDOM = "random";
         String OPTION_COUNT = "count";
    }

    interface CxnReactor {
        String CLASSNAME = "org.squonk.execution.steps.impl.CxnReactorStep";
        String VARIABLE_R1 = "R1";
        String VARIABLE_R2 = "R2";
        String OPTION_REACTION = "reaction";
        String OPTION_IGNORE_REACTIVITY = "ignoreReactivityRules";
        String OPTION_IGNORE_SELECTIVITY = "ignoreSelectivityRules";
        String OPTION_IGNORE_TOLERANCE = "ignoreToleranceRules";
        String OPTION_REACTOR_OUTPUT = "reactorOutput";
    }
    
}
