/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.execution.steps;

/**
 *
 * @author timbo
 */
public interface StepDefinitionConstants {

    String STEP_DATASET_WRITER = "org.squonk.execution.steps.impl.DatasetWriterStep";
    String STEP_DATASET_READER = "org.squonk.execution.steps.impl.DatasetReaderStep";

    String VARIABLE_INPUT_DATASET = "input";
    String VARIABLE_OUTPUT_DATASET = "output";
    String VARIABLE_FILE_INPUT = "fileContent";

    String OPTION_BODY = "body";
    String OPTION_BODY_CONTENT_TYPE = "body-content-type";
    String OPTION_SERVICE_ENDPOINT = "serviceEndpoint"; // the URL for the service
    String OPTION_MEDIA_TYPE_INPUT = "inputMediaType"; // the format for the input
    String OPTION_MEDIA_TYPE_OUTPUT = "outputMediaType"; // the format for the output
    String OPTION_DOCKER_IMAGE = "dockerImage";
    String OPTION_DOCKER_IMAGE_VERSION = "dockerImageVersion";

    interface MoleculeServiceThinExecutor {
        String CLASSNAME = "org.squonk.execution.steps.impl.MoleculeServiceThinExecutorStep";
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

    interface DatasetEnricher {
        String CLASSNAME = "org.squonk.execution.steps.impl.DatasetEnricherStep";
        String VARIABLE_NEW_DATA = "newData";
        String OPTION_MAIN_FIELD = "mainField";
        String OPTION_EXTRA_FIELD = "extraField";
        String OPTION_MERGE_MODE = "mergeMode";;
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

    interface StructureUpload {
        String CLASSNAME = "org.squonk.execution.steps.impl.StructureReaderStep";
        String OPTION_FILE_UPLOAD = "fileUpload";
        String OPTION_FILE_FORMAT = "fileFormat";
    }

    interface PdbUpload {
        String CLASSNAME = "org.squonk.execution.steps.impl.PdbReaderStep";
        String OPTION_FILE_UPLOAD = "pdbUpload";
        String VARIABLE_PDB = "pdb";
    }

    interface Mol2Upload {
        String CLASSNAME = "org.squonk.execution.steps.impl.Mol2ReaderStep";
        String OPTION_FILE_UPLOAD = "mol2Upload";
        String VARIABLE_MOL2 = "mol2";
    }

    interface ZipFileUpload {
        String CLASSNAME = "org.squonk.execution.steps.impl.ZipFileReaderStep";
        String OPTION_FILE_UPLOAD = "zipUpload";
        String VARIABLE_ZIP = "zip";
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

    interface DatasetUUIDFilter {
        String CLASSNAME = "org.squonk.execution.steps.impl.DatasetUUIDFilterStep";
        String OPTION_UUIDS = "uuids";
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

    interface DataTransformPotion {
        String CLASSNAME = "org.squonk.execution.steps.impl.DataTransformPotionStep";
        String OPTION_POTION = "potion";
    }

    interface DatasetSorter {
        String CLASSNAME = "org.squonk.execution.steps.impl.DatasetSorterStep";
        String OPTION_DIRECTIVES = "directives";
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

    interface DatasetMoleculesFromFieldStep {
        String CLASSNAME = "org.squonk.execution.steps.impl.DatasetMoleculesFromFieldStep";
        String OPTION_MOLECULES_FIELD = "moleculesField";
    }

    interface SmilesStructures {
        String CLASSNAME = "org.squonk.execution.steps.impl.SmilesStructuresStep";
        String OPTION_SMILES = "smiles";
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
        String OPTION_OUTPUT_FORMAT = "outputFormat";
    }
    
}
