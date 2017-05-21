package org.squonk.core.service.discovery;

import org.squonk.core.*;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.impl.GenericDatasetDockerExecutorStep;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.io.IORoute;
import org.squonk.options.DatasetFieldTypeDescriptor;
import org.squonk.options.DatasetsFieldOptionDescriptor;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.util.CommonMimeTypes;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by timbo on 13/03/17.
 */
public class StandardServiceDescriptors {


    private ServiceDescriptorSet services = new ServiceDescriptorSet(null, null, null);

    StandardServiceDescriptors() {

        services = new ServiceDescriptorSet(null, null, Arrays.asList(new ServiceDescriptor[]{

                // this one allows to execute arbitary script in the specified docker container
                new DockerServiceDescriptor("docker.generic.dataset.v1", "GenericDockerProcessDataset", "Process Dataset with command in Docker container",
                        new String[]{"program", "code", "dataset", "docker"},
                        null,
                        "icons/program.png",
                        ServiceConfig.Status.ACTIVE,
                        new Date(),
                        IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
                        new IORoute[]{IORoute.FILE},
                        IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                        new IORoute[]{IORoute.FILE},
                        new OptionDescriptor[]{
                                new OptionDescriptor<>(String.class, StepDefinitionConstants.OPTION_DOCKER_IMAGE,
                                        "Docker image name", "The Docker image to use", OptionDescriptor.Mode.User)
                                        .withMinMaxValues(1, 1),
                                new OptionDescriptor<>(String.class, "inputMediaType",
                                        "Input media type", "The format the input will be written as e.g. application/x-squonk-dataset-molecule+json", OptionDescriptor.Mode.User)
                                        .withValues(new String[]{CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, CommonMimeTypes.MIME_TYPE_MDL_SDF})
                                        .withDefaultValue(CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                                        .withMinMaxValues(1, 1),
                                new OptionDescriptor<>(String.class, "outputMediaType",
                                        "Output media type", "The format the output will be read as e.g. chemical/x-mdl-sdfile", OptionDescriptor.Mode.User)
                                        .withValues(new String[]{CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, CommonMimeTypes.MIME_TYPE_MDL_SDF})
                                        .withDefaultValue(CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON)
                                        .withMinMaxValues(1, 1),
                                new OptionDescriptor<>(new MultiLineTextTypeDescriptor(20, 60, MultiLineTextTypeDescriptor.MIME_TYPE_SCRIPT_SHELL),
                                        StepDefinitionConstants.DockerProcessDataset.OPTION_DOCKER_COMMAND,
                                        "Command", "The command to be executed e.g. bash script to execute inside container", OptionDescriptor.Mode.User)
                                        .withMinMaxValues(1, 1)
                        },
                        null, // thin descriptors
                        GenericDatasetDockerExecutorStep.class.getName(), // executor
                        null, // image - defined in option
                        null, // command - defined in option
                        null  // volumes
                ),

                new DefaultServiceDescriptor("core.dataset.filter.slice.v1", "Dataset slice selector", "Generate a defined slice of the dataset",
                        new String[]{"filter", "slice", "dataset"},
                        null, "icons/filter.png",
                        ServiceConfig.Status.ACTIVE,
                        new Date(),
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                        new OptionDescriptor[]{
                                new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectSlice.OPTION_SKIP, "Number to skip", "The number of records to skip", OptionDescriptor.Mode.User),
                                new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectSlice.OPTION_COUNT, "Number to include", "The number of records to include after skipping", OptionDescriptor.Mode.User)
                        },
                        null,
                        StepDefinitionConstants.DatasetSelectSlice.CLASSNAME
                ),

                new DefaultServiceDescriptor("core.dataset.filter.random.v1", "Dataset random selector", "Generate a random slice of the dataset",
                        new String[]{"filter", "random", "dataset"},
                        null, "icons/filter.png",
                        ServiceConfig.Status.ACTIVE,
                        new Date(),
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                        new OptionDescriptor[]{
                                new OptionDescriptor<>(Float.class, StepDefinitionConstants.DatasetSelectRandom.OPTION_RANDOM, "Random fraction (0-1)", "The fraction of records to randomly select (between 0 and 1, default 0.001)", OptionDescriptor.Mode.User),
                                new OptionDescriptor<>(Integer.class, StepDefinitionConstants.DatasetSelectRandom.OPTION_COUNT, "Max records", "The max number of records to include, default 1000", OptionDescriptor.Mode.User)
                        },
                        null,
                        StepDefinitionConstants.DatasetSelectRandom.CLASSNAME
                ),

                new DefaultServiceDescriptor("core.dataset.merger.v1", "DatasetMerger", "Merge datasets into one",
                        new String[]{"merge", "dataset"},
                        null, "icons/merge.png",
                        ServiceConfig.Status.ACTIVE,
                        new Date(),
                        new IODescriptor[]{
                                // this is done backwards as JSPlumb displays the endpoints in reverse order.
                                IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 5),
                                IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 4),
                                IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 3),
                                IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 2),
                                IODescriptors.createMoleculeObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET + 1)
                        },
                        // TODO - find a way to dynamically determine the output type based on the first bound input
                        // the consequence of no tdoing this is that themerger cell always reports its output as BasicObject
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                        new OptionDescriptor[]{

                                new DatasetsFieldOptionDescriptor(StepDefinitionConstants.DatasetMerger.OPTION_MERGE_FIELD_NAME,
                                        "Merge field name", "Name of value field which identifies equivalent entries"),

                                new OptionDescriptor<>(Boolean.class, StepDefinitionConstants.DatasetMerger.OPTION_KEEP_FIRST, "When duplicate keep first",
                                        "When duplicate field name use the existing value rather than the new one", OptionDescriptor.Mode.User)
                                        .withDefaultValue(true)
                        },
                        null,
                        StepDefinitionConstants.DatasetMerger.CLASSNAME
                ),

                new DefaultServiceDescriptor("core.dataset.enricher.v1", "DatasetEnricher", "Enrich a dataset with content from another dataset",
                        new String[]{"enrich", "merge", "dataset"},
                        null, "icons/merge.png",
                        ServiceConfig.Status.ACTIVE,
                        new Date(),
                        new IODescriptor[]{
                                IODescriptors.createBasicObjectDataset(StepDefinitionConstants.DatasetEnricher.VARIABLE_NEW_DATA),
                                IODescriptors.createBasicObjectDataset(StepDefinitionConstants.VARIABLE_INPUT_DATASET)
                        },
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                        new OptionDescriptor[]{

                                new OptionDescriptor<>(new DatasetFieldTypeDescriptor(StepDefinitionConstants.VARIABLE_INPUT_DATASET, new Class[]{String.class, Integer.class}),
                                        StepDefinitionConstants.DatasetEnricher.OPTION_MAIN_FIELD, "Input field name",
                                        "Name of field in input which identifies equivalent entries", OptionDescriptor.Mode.User)
                                        .withMinMaxValues(0, 1),

                                new OptionDescriptor<>(new DatasetFieldTypeDescriptor(StepDefinitionConstants.DatasetEnricher.VARIABLE_NEW_DATA, new Class[]{String.class, Integer.class}),
                                        StepDefinitionConstants.DatasetEnricher.OPTION_EXTRA_FIELD, "NewData field name",
                                        "Name of field in newData which identifies equivalent entries", OptionDescriptor.Mode.User)
                                        .withMinMaxValues(0, 1),

                                new OptionDescriptor<>(String.class, StepDefinitionConstants.DatasetEnricher.OPTION_MERGE_MODE, "Mode: main, values or both",
                                        "Merge the main content (e.g the structure for molecules), the values or both", OptionDescriptor.Mode.User)
                                        .withValues(new String[]{"main", "values", "both"})
                                        .withDefaultValue("both")
                                        .withMinMaxValues(1, 1)

                        },
                        null,
                        StepDefinitionConstants.DatasetEnricher.CLASSNAME
                ),

                new DefaultServiceDescriptor("core.dataset.uuidfilter.v1", "DatasetUUIDFilter", "Filter a dataset for a set of UUIDs",
                        new String[]{"filter", "uuid", "dataset"},
                        null, "icons/filter.png",
                        ServiceConfig.Status.ACTIVE,
                        new Date(),
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
                        IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
                        new OptionDescriptor[]{

                                new OptionDescriptor<>(new MultiLineTextTypeDescriptor(10, 60, CommonMimeTypes.MIME_TYPE_TEXT_PLAIN),
                                        StepDefinitionConstants.DatasetUUIDFilter.OPTION_UUIDS,
                                        "UUIDs", "List of UUIDs", OptionDescriptor.Mode.User)
                                        .withMinMaxValues(1, 1)

                        },
                        null,
                        StepDefinitionConstants.DatasetUUIDFilter.CLASSNAME
                )
        }));

    }

    protected ServiceDescriptorSet get() {
        return services;
    }


}
