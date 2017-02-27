package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetUtils;
import org.squonk.dataset.ThinDatasetWrapper;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/** Handles thin Docker execution.
 * The thin execution requires a ThinDescriptor to be defined as part of the DockerServiceDescriptor. That ThinDescriptor
 * defines that a certain input is mapped to a certain output for thin execution. Typically there is one input (named "input")
 * and one output (names "output") but multiple inputs and outputs are supported but an input can only be mapped to a single output.
 * Also, currently thin execution is only implemented where the input and output are both Datasets.
 *
 * In the special case of there being a single input and a single output and no ThinDescriptor has been defined in the DockerServiceDescriptor
 * then a ThinDescriptor with default parameters is created and used. This avoids the need to specifically define a ThinDescriptor
 * with default args.
 *
 *
 * Created by timbo on 23/02/17.
 */
public class ThinDatasetDockerExecutorStep extends DefaultDockerExecutorStep {

    private static final Logger LOG = Logger.getLogger(ThinDatasetDockerExecutorStep.class.getName());

    /** ThinDescriptors keyed by input name */
    private Map<String,ThinDescriptor> thinDescriptors = new HashMap<>();

    /** ThinDatasetWrappers keys by output names */
    private Map<String,ThinDatasetWrapper> wrappers = new HashMap<>();


    @Override
    protected void handleInputs(CamelContext camelContext, DockerServiceDescriptor serviceDescriptor, VariableManager varman, DockerRunner runner) throws Exception {

        ServiceConfig serviceConfig = serviceDescriptor.getServiceConfig();
        IODescriptor[] inputDescriptors = serviceConfig.getInputDescriptors();
        if (inputDescriptors != null) {
            LOG.info("Handling " + inputDescriptors.length + " inputs");
            for (IODescriptor d : inputDescriptors) {
                ThinDescriptor td = findThinDescriptorForInput(serviceDescriptor, d.getName());
                if (td != null) {
                    thinDescriptors.put(d.getName(), td);
                } else if (serviceConfig.getInputDescriptors().length == 1 && serviceConfig.getOutputDescriptors().length == 1) {
                    LOG.info("Creating default ThinDescriptor");
                    // special case where there is 1 input and 1 output and no ThinDescriptor defined so we create one with default params
                    thinDescriptors.put(d.getName(), new ThinDescriptor(d.getName(), serviceConfig.getOutputDescriptors()[0].getName()));
                }
            }
        }

        super.handleInputs(camelContext, serviceDescriptor, varman, runner);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <P,Q> void handleInput(CamelContext camelContext, DockerServiceDescriptor serviceDescriptor, VariableManager varman, DockerRunner runner, IODescriptor<P,Q> ioDescriptor) throws Exception {

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
            super.handleInput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
        }
    }

    private ThinDescriptor findThinDescriptorForInput(DockerServiceDescriptor serviceDescriptor, String inputName) {
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
    protected <P,Q> void handleOutput(CamelContext camelContext, DockerServiceDescriptor serviceDescriptor, VariableManager varman, DockerRunner runner, IODescriptor<P,Q> ioDescriptor) throws Exception {

        ThinDatasetWrapper wrapper = wrappers.get(ioDescriptor.getName());
        if (ioDescriptor.getPrimaryType() == Dataset.class && wrapper != null) {
            LOG.info("Handling thin output for " + ioDescriptor.getName());
            FilesystemReadContext context = new FilesystemReadContext(runner.getHostWorkDir(), ioDescriptor.getName());
            Dataset thin = varman.getValue(Dataset.class, context);
            Dataset thick = wrapper.generateOutput(thin);
            createMappedOutput(ioDescriptor.getName(), Dataset.class, thick, varman);
        } else {
            super.handleOutput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
        }
    }
}
