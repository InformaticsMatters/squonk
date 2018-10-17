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

import org.apache.camel.CamelContext;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetUtils;
import org.squonk.dataset.ThinDatasetWrapper;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.types.io.JsonHandler;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * Also provides the mechanism for handling thin dataset execution, though sub-classes must invoke this by overriding the suitable
 * methods and calling the 'Thin' equivalents. See {@link org.squonk.execution.steps.impl.ThinDatasetDockerExecutorStep} as an example.
 *
 * The thin execution requires a ThinDescriptor to be defined as part of the DockerServiceDescriptor. That ThinDescriptor
 * defines that a certain input is mapped to a certain output for thin execution. Typically there is one input (named "input")
 * and one output (names "output") but multiple inputs and outputs are supported but an input can only be mapped to a single output.
 * Also, currently thin execution is only implemented where the input and output are both Datasets.
 * <p>
 * In the special case of there being a single input and a single output and no ThinDescriptor has been defined in the DockerServiceDescriptor
 * then a ThinDescriptor with default parameters is created and used. This avoids the need to specifically define a ThinDescriptor
 * with default args.
 *
 * Created by timbo on 20/06/17.
 */
public abstract class AbstractThinDatasetStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(AbstractThinDatasetStep.class.getName());

    /** ThinDescriptors keyed by input name */
    private Map<String,ThinDescriptor> thinDescriptors = new HashMap<>();

    /** ThinDatasetWrappers keys by output names */
    private Map<String,ThinDatasetWrapper> wrappers = new HashMap<>();


    protected ThinDescriptor getThinDescriptor(IODescriptor inputDescriptor) {
        ThinDescriptor[] tds = getHttpServiceDescriptor().getThinDescriptors();
        ServiceConfig serviceConfig = getHttpServiceDescriptor().getServiceConfig();
        ThinDescriptor td;
        if (tds == null || tds.length == 0) {
            if (inputDescriptor.getPrimaryType() == Dataset.class) {
                td = new ThinDescriptor(inputDescriptor.getName(), serviceConfig.getOutputDescriptors()[0].getName());
            } else {
                throw new IllegalStateException("Thin execution only suppported for Dataset. Found " + inputDescriptor.getPrimaryType().getName());
            }
        } else if (tds.length == 1) {
            if (tds[0] == null) {
                LOG.warning("ThinDescriptor array provided including a null element. This is bad practice and can lead to problems.");
                td = new ThinDescriptor(inputDescriptor.getName(), serviceConfig.getOutputDescriptors()[0].getName());
            } else {
                td = tds[0];
            }
        } else {
            throw new IllegalStateException("Expected single ThinDescriptor but found " + tds.length);
        }
        return td;
    }


    protected void handleThinInputs(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner) throws Exception {

        IODescriptor[] inputDescriptors = serviceDescriptor.resolveInputIODescriptors();
        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        if (inputDescriptors != null) {
            LOG.info("Handling " + inputDescriptors.length + " inputs");
            for (IODescriptor d : inputDescriptors) {
                ThinDescriptor td = findThinDescriptorForInput(serviceDescriptor, d.getName());
                if (td != null) {
                    thinDescriptors.put(d.getName(), td);
                } else if (inputDescriptors.length == 1 && outputDescriptors.length == 1) {
                    LOG.info("Creating default ThinDescriptor");
                    // special case where there is 1 input and 1 output and no ThinDescriptor defined so we create one with default params
                    thinDescriptors.put(d.getName(), new ThinDescriptor(d.getName(), outputDescriptors[0].getName()));
                }
            }
        }

        doHandleInputs(camelContext, serviceDescriptor, varman, runner);
    }

    @SuppressWarnings("unchecked")
    protected <P,Q> void handleThinInput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner,
            IODescriptor<P,Q> ioDescriptor) throws Exception {

        LOG.info("Handling input for " + ioDescriptor);
        ThinDescriptor td = thinDescriptors.get(ioDescriptor.getName());
        if (ioDescriptor.getPrimaryType() == Dataset.class && td != null) {
            LOG.info("Thin execution: " + td.toString());

            ThinDatasetWrapper wrapper = DatasetUtils.createThinDatasetWrapper(td, ioDescriptor.getSecondaryType(), options);

            if (td.getOutput() != null) {
                // put the wrapper to the map under the output name so that it can be used when the corresponding output is processed
                wrappers.put(td.getOutput(), wrapper);
            }

            // process the input to make it thin
            Dataset thick = fetchMappedInput(ioDescriptor.getName(), Dataset.class, ioDescriptor.getSecondaryType(), varman, true);
            Dataset thin = wrapper.prepareInput(thick);

            File hostWorkDir = runner.getHostWorkDir();
            LOG.info("Handling thin input for " + ioDescriptor.getName() + " in " + hostWorkDir.toString());
            FilesystemWriteContext context = new FilesystemWriteContext(hostWorkDir, ioDescriptor.getName());
            varman.putValue(Dataset.class, thin, context);
        } else {
            doHandleInput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
        }
    }

    private ThinDescriptor findThinDescriptorForInput(DefaultServiceDescriptor serviceDescriptor, String inputName) {
        ThinDescriptor[] descriptors = serviceDescriptor.getThinDescriptors();
        if (descriptors != null && descriptors.length > 0) {
            for (ThinDescriptor descriptor : descriptors) {
                if (inputName.equalsIgnoreCase(descriptor.getInput())) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <P,Q> void handleThinOutput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner,
            IODescriptor<P,Q> ioDescriptor) throws Exception {

        ThinDatasetWrapper wrapper = wrappers.get(ioDescriptor.getName());
        if (ioDescriptor.getPrimaryType() == Dataset.class && wrapper != null) {
            File hostWorkDir = runner.getHostWorkDir();
            LOG.info("Handling thin output for " + ioDescriptor.getName() + " in " + hostWorkDir.toString());
            FilesystemReadContext context = new FilesystemReadContext(hostWorkDir, ioDescriptor.getName());
            // TODO - determine the secondary type
            Dataset thin = varman.getValue(Dataset.class, null, context);
            Dataset thick = wrapper.generateOutput(thin);
            createMappedOutput(ioDescriptor.getName(), Dataset.class, thick, varman);
        } else {
            doHandleOutput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
        }
    }

    /** Handle thin execution where there is a single input and single output dataset
     *
     * @param varman
     * @param context
     * @throws Exception
     */
    protected void doExecuteThinDataset(VariableManager varman, CamelContext context) throws Exception {
        updateStatus(MSG_PREPARING_INPUT);

        Dataset inputDataset = fetchMappedInput(StepDefinitionConstants.VARIABLE_INPUT_DATASET, Dataset.class, varman);

        IODescriptor inputDescriptor = getSingleInputDescriptor();
        ThinDescriptor td = getThinDescriptor(inputDescriptor);
        if (td == null) {
            throw new IllegalStateException("No ThinDescriptor was provided of could be inferred");
        }
        ThinDatasetWrapper thinWrapper = DatasetUtils.createThinDatasetWrapper(td, inputDescriptor.getSecondaryType(), options);
        Dataset thinDataset = thinWrapper.prepareInput(inputDataset);

        Map<String,Object> results = executeWithData(Collections.singletonMap(StepDefinitionConstants.VARIABLE_INPUT_DATASET, thinDataset), context);
        Dataset responseResults = getSingleDatasetFromMap(results);
        Dataset resultDataset = thinWrapper.generateOutput(responseResults);

        createMappedOutput(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET, Dataset.class, resultDataset, varman);
        statusMessage = generateStatusMessage(inputDataset.getSize(), resultDataset.getSize(), -1);
        LOG.info("Results: " + JsonHandler.getInstance().objectToJson(resultDataset.getMetadata()));
    }


}
