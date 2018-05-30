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
import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.core.ServiceDescriptor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetUtils;
import org.squonk.dataset.ThinDatasetWrapper;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.util.GroovyUtils;
import org.squonk.execution.variable.VariableManager;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * Also provides the mechanism for handling thin dataset execution, though superclasses must invoke this by overriding the suitable
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
public abstract class AbstractServiceStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(AbstractServiceStep.class.getName());

    protected ServiceDescriptor serviceDescriptor;

    /** ThinDescriptors keyed by input name */
    private Map<String,ThinDescriptor> thinDescriptors = new HashMap<>();

    /** ThinDatasetWrappers keys by output names */
    private Map<String,ThinDatasetWrapper> wrappers = new HashMap<>();

    @Override
    public IODescriptor[] getInputs() {
        return serviceDescriptor.resolveInputIODescriptors();
    }

    @Override
    public IODescriptor[] getOutputs() {
        return serviceDescriptor.resolveOutputIODescriptors();
    }

    @Override
    public void configure(
            Long outputProducerId,
            String jobId,
            Map<String, Object> options,
            IODescriptor[] inputs,
            IODescriptor[] outputs,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings) {

        throw new IllegalStateException(this.getClass().getCanonicalName() + " must provide ServiceDescriptor if no default one is defined");

    }

    @Override
    public void configure(
            Long outputProducerId,
            String jobId,
            Map<String, Object> options,
            Map<String, VariableKey> inputVariableMappings,
            Map<String, String> outputVariableMappings,
            ServiceDescriptor serviceDescriptor) {
        this.outputProducerId = outputProducerId;
        this.jobId = jobId;
        this.options = options;
        this.inputVariableMappings.putAll(inputVariableMappings);
        this.outputVariableMappings.putAll(outputVariableMappings);
        this.serviceDescriptor = serviceDescriptor;
    }

    protected HttpServiceDescriptor getHttpServiceDescriptor() {
        if (serviceDescriptor == null) {
            throw new IllegalStateException("Service descriptor not found");
        } else if (!(serviceDescriptor instanceof HttpServiceDescriptor)) {
            throw new IllegalStateException("Invalid service descriptor. Expected HttpServiceDescriptor but found " + serviceDescriptor.getClass().getSimpleName());
        }
        return (HttpServiceDescriptor)serviceDescriptor;
    }

    protected String getHttpExecutionEndpoint() {
        return getHttpServiceDescriptor().getExecutionEndpoint();
    }

    protected IODescriptor getSingleInputDescriptor() {
        ServiceConfig serviceConfig = getHttpServiceDescriptor().getServiceConfig();
        IODescriptor[] inputDescriptors = serviceConfig.getInputDescriptors();
        IODescriptor inputDescriptor;
        if (inputDescriptors != null && inputDescriptors.length == 1) {
            inputDescriptor = inputDescriptors[0];
        } else if (inputDescriptors == null || inputDescriptors.length == 0 ) {
            throw new IllegalStateException("Expected one input IODescriptor. Found none");
        } else {
            throw new IllegalStateException("Expected one input IODescriptor. Found " + inputDescriptors.length);
        }
        return inputDescriptor;
    }

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

    protected void handleInputs(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner) throws Exception {
        doHandleInputs(camelContext, serviceDescriptor, varman, runner);
    }

    protected void doHandleInputs(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner) throws Exception {
        IODescriptor[] inputDescriptors = serviceDescriptor.resolveInputIODescriptors();
        if (inputDescriptors != null) {
            LOG.info("Handling " + inputDescriptors.length + " inputs");
            for (IODescriptor d : inputDescriptors) {
                LOG.info("Writing input for " + d.getName() + " " + d.getMediaType());
                handleInput(camelContext, serviceDescriptor, varman, runner, d);
            }
        }
    }

    protected <P,Q> void handleInput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner,
            IODescriptor<P,Q> ioDescriptor) throws Exception {
        doHandleInput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
    }

    protected <P,Q> void doHandleInput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner,
            IODescriptor<P,Q> ioDescriptor) throws Exception {
        P value = fetchMappedInput(ioDescriptor.getName(), ioDescriptor.getPrimaryType(), varman, true);
        File dir = runner.getHostWorkDir();
        FilesystemWriteContext writeContext = new FilesystemWriteContext(dir, ioDescriptor.getName());
        varman.putValue(ioDescriptor.getPrimaryType(), value, writeContext);
    }

    protected <P,Q> void handleOutputs(CamelContext camelContext, DefaultServiceDescriptor serviceDescriptor, VariableManager varman, ContainerRunner runner) throws Exception {
        doHandleOutputs(camelContext, serviceDescriptor, varman, runner);
    }

    protected <P,Q> void doHandleOutputs(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner) throws Exception {

        IODescriptor[] outputDescriptors = serviceDescriptor.resolveOutputIODescriptors();
        if (outputDescriptors != null) {
            LOG.info("Handling " + outputDescriptors.length + " outputs");
            for (IODescriptor d : outputDescriptors) {
                handleOutput(camelContext, serviceDescriptor, varman, runner, d);
            }
        }
    }

    protected <P,Q> void handleOutput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner,
            IODescriptor<P,Q> ioDescriptor) throws Exception {
        doHandleOutput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
    }
    protected <P,Q> void doHandleOutput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            ContainerRunner runner,
            IODescriptor<P,Q> ioDescriptor) throws Exception {

        FilesystemReadContext readContext = new FilesystemReadContext(runner.getHostWorkDir(), ioDescriptor.getName());
        P value = varman.getValue(ioDescriptor.getPrimaryType(), readContext);
        createMappedOutput(ioDescriptor.getName(), ioDescriptor.getPrimaryType(), value, varman);
    }

    /** Take the command template and substitute it with the user specified options.
     * The options are those that begin with 'arg.'
     * The template is a Groovy GString.
     *
     * @param cmdTemplate
     * @param options
     * @return
     */
    protected String expandCommand(String cmdTemplate, Map<String,Object> options) {
        Map<String, Object> args = new LinkedHashMap<>();
        // Inject magical variables that are used to define locations of inputs and outputs.
        // For execution these are set to the empty string.
        args.put("PIN", "");
        args.put("POUT", "");
        options.forEach((k, v) -> {
            if (k.startsWith("arg.")) {
                LOG.info("Found argument " + k + " = " + v);
                args.put(k.substring(4), v);
            }
        });

        // replace windows line end characters
        String command = cmdTemplate.replaceAll("\\r\\n", "\n");
        LOG.info("Template: " + command);
        String expandedCommand = GroovyUtils.expandTemplate(command, args);
        LOG.info("Command: " + expandedCommand);
        return expandedCommand;
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
            Dataset thick = fetchMappedInput(ioDescriptor.getName(), Dataset.class, varman, true);
            Dataset thin = wrapper.prepareInput(thick);

            FilesystemWriteContext context = new FilesystemWriteContext(runner.getHostWorkDir(), ioDescriptor.getName());
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
            LOG.info("Handling thin output for " + ioDescriptor.getName());
            FilesystemReadContext context = new FilesystemReadContext(runner.getHostWorkDir(), ioDescriptor.getName());
            Dataset thin = varman.getValue(Dataset.class, context);
            Dataset thick = wrapper.generateOutput(thin);
            createMappedOutput(ioDescriptor.getName(), Dataset.class, thick, varman);
        } else {
            doHandleOutput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
        }
    }

}
