/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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
import org.squonk.io.IODescriptor;
import org.squonk.types.BasicObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * Also provides the mechanism for handling thin execution, though sub-classes must invoke this by setting the enableThinExecution
 * property to true (it is set to false by default).
 * See {@link org.squonk.execution.steps.impl.ThinDatasetDockerExecutorStep} as an example.
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
public abstract class AbstractThinStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(AbstractThinStep.class.getName());

    /** ThinDescriptors keyed by input name */
    private Map<String,ThinDescriptor> thinDescriptors = new HashMap<>();

    /** ThinDatasetWrappers keys by output names */
    private Map<String,ThinDatasetWrapper> wrappers = new HashMap<>();

    protected boolean enableThinExecution = false;


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


    @Override
    protected Map<String,Object> prepareInputs(Map<String,Object> inputs) throws Exception {

        if (!enableThinExecution) {
            return inputs;
        }

        DefaultServiceDescriptor serviceDescriptor = getDefaultServiceDescriptor();

        IODescriptor[] inputDescriptors = serviceDescriptor.resolveInputIODescriptors();
        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        Map<String,Object> results = new HashMap<>();
        if (inputDescriptors != null) {
            LOG.info("Handling " + inputDescriptors.length + " inputs");
            for (IODescriptor iod : inputDescriptors) {
                Object value = inputs.get(iod.getName());
                ThinDescriptor td = findThinDescriptorForInput(serviceDescriptor, iod.getName());
                if (td != null) {
                    LOG.info("Found ThinDescriptor for input " + iod.getName());
                    thinDescriptors.put(iod.getName(), td);
                } else if (value instanceof Dataset && inputDescriptors.length == 1 && outputDescriptors.length == 1) {
                    LOG.info("Creating default ThinDescriptor for input " + iod.getName());
                    // special case where there is 1 input and 1 output and no ThinDescriptor defined so we create one with default params
                    td = new ThinDescriptor(iod.getName(), outputDescriptors[0].getName());
                    thinDescriptors.put(iod.getName(), td);
                }
                if (td != null) {
                    if (value instanceof Dataset) {
                        Dataset thin = prepareInput((Dataset) value, camelContext, serviceDescriptor, iod);
                        results.put(iod.getName(), thin);
                    } else {
                        throw new IllegalStateException("Only Datasets support thin execution");
                    }
                } else {
                    results.put(iod.getName(), value);
                }
            }
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    protected Dataset prepareInput(
            Dataset input,
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            IODescriptor ioDescriptor) throws Exception {

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

            Dataset thin = wrapper.prepareInput(input);
            return thin;

        } else {
            return input;
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

    @Override
    @SuppressWarnings("unchecked")
        protected Map<String,Object> prepareOutputs(Map<String,Object> outputs) throws Exception {

        if (!enableThinExecution) {
            return outputs;
        }

        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        Map<String,Object> results = new LinkedHashMap<>(outputs.size());
        if (outputDescriptors != null) {
            LOG.info("Handling " + outputDescriptors.length + " outputs");
            for (IODescriptor iod : outputDescriptors) {
                String name = iod.getName();
                Object value = outputs.get(name);
                if (value == null) {
                    LOG.warning("Encountered null output for " + name);
                } else {
                    Object result = prepareOutput(value, camelContext, iod);
                    results.put(name, result);
                }
            }
        }
        return results;
    }


    @SuppressWarnings("unchecked")
    protected Object prepareOutput(
            Object output,
            CamelContext camelContext,
            IODescriptor iod) throws Exception {

        ThinDatasetWrapper wrapper = wrappers.get(iod.getName());
        if (iod.getPrimaryType() == Dataset.class && wrapper != null) {
            Dataset thick = wrapper.generateOutput((Dataset)output);
            return thick;
        } else {
            return output;
        }
    }
}
