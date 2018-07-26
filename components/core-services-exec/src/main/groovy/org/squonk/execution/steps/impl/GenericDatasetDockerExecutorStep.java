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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.io.IODescriptors;
import org.squonk.io.IORoute;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.util.CommonMimeTypes;

import java.util.Date;
import java.util.logging.Logger;


/**
 * Handles generic execution of a script (e.g. bash or python) in a Docker container.
 * Inputs and outputs Dataset&lt;MoleculeObject&gt; and supports a limited number of format conversions. Currently the only
 * conversions are to and from SDF.
 * <p>
 * Created by timbo on 23/02/17.
 */
public class GenericDatasetDockerExecutorStep extends DefaultDockerExecutorStep {

    private static final Logger LOG = Logger.getLogger(GenericDatasetDockerExecutorStep.class.getName());

    public static final DockerServiceDescriptor SERVICE_DESCRIPTOR = new DockerServiceDescriptor("docker.generic.dataset.v1", "GenericDockerProcessDataset", "Process Dataset with command in Docker container",
            new String[]{"program", "code", "dataset", "docker"},
            null,
            "icons/program.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            new IORoute[]{new IORoute(IORoute.Route.FILE)},
            IODescriptors.createMoleculeObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new IORoute[]{new IORoute(IORoute.Route.FILE)},
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
    );


    @Override
    @SuppressWarnings("unchecked")
    protected <P, Q> void handleInput(CamelContext camelContext, DefaultServiceDescriptor serviceDescriptor, VariableManager varman, ContainerRunner runner, IODescriptor<P, Q> ioDescriptor) throws Exception {

        String inputType = (String) options.get(StepDefinitionConstants.OPTION_MEDIA_TYPE_INPUT);
        IODescriptor writeAs = generateIODescriptorForMediaType(inputType, ioDescriptor);

        FilesystemWriteContext writeContext = new FilesystemWriteContext(runner.getHostWorkDir(), writeAs.getName());
        P value = fetchMappedInput(ioDescriptor.getName(), ioDescriptor.getPrimaryType(), ioDescriptor.getSecondaryType(), varman, true);
        Object converted = convertValue(camelContext, ioDescriptor, writeAs, value);
        varman.putValue(writeAs.getPrimaryType(), converted, writeContext);
    }


    @Override
    @SuppressWarnings("unchecked")
    protected <P, Q> void handleOutput(CamelContext camelContext, DefaultServiceDescriptor serviceDescriptor, VariableManager varman, ContainerRunner runner, IODescriptor<P, Q> ioDescriptor) throws Exception {

        String outputType = (String) options.get(StepDefinitionConstants.OPTION_MEDIA_TYPE_OUTPUT);
        IODescriptor readAs = generateIODescriptorForMediaType(outputType, ioDescriptor);

        FilesystemReadContext readContext = new FilesystemReadContext(runner.getHostWorkDir(), readAs.getName());
        Object value = varman.getValue(readAs.getPrimaryType(), readAs.getSecondaryType(), readContext);
        P converted = (P) convertValue(camelContext, readAs, ioDescriptor, value);
        createMappedOutput(ioDescriptor.getName(), ioDescriptor.getPrimaryType(), converted, varman);
    }

}
