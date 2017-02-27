package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.DockerServiceDescriptor;
import org.squonk.execution.docker.DockerRunner;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.variable.VariableManager;
import org.squonk.execution.variable.impl.FilesystemReadContext;
import org.squonk.execution.variable.impl.FilesystemWriteContext;
import org.squonk.io.IODescriptor;

import java.util.logging.Logger;


/** Handles generic execution of a script (e.g. bash or python) in a Docker container.
 * Inputs and outputs Dataset&lt;MoleculeObject&gt; and supports a limited number of format conversions. Currently the only
 * conversions are to and from SDF.
 *
 * Created by timbo on 23/02/17.
 */
public class GenericDatasetDockerExecutorStep extends DefaultDockerExecutorStep {

    private static final Logger LOG = Logger.getLogger(GenericDatasetDockerExecutorStep.class.getName());


    @Override
    @SuppressWarnings("unchecked")
    protected <P, Q> void handleInput(CamelContext camelContext, DockerServiceDescriptor serviceDescriptor, VariableManager varman, DockerRunner runner, IODescriptor<P, Q> ioDescriptor) throws Exception {

        String inputType = (String) options.get(StepDefinitionConstants.OPTION_MEDIA_TYPE_INPUT);
        IODescriptor writeAs = generateIODescriptorForMediaType(inputType, ioDescriptor);

        FilesystemWriteContext writeContext = new FilesystemWriteContext(runner.getHostWorkDir(), writeAs.getName());
        P value = fetchMappedInput(ioDescriptor.getName(), ioDescriptor.getPrimaryType(), varman, true);
        Object converted = convertValue(camelContext, ioDescriptor, writeAs, value);
        varman.putValue(writeAs.getPrimaryType(), converted, writeContext);
    }


    @Override
    @SuppressWarnings("unchecked")
    protected <P, Q> void handleOutput(CamelContext camelContext, DockerServiceDescriptor serviceDescriptor, VariableManager varman, DockerRunner runner, IODescriptor<P, Q> ioDescriptor) throws Exception {

        String outputType = (String) options.get(StepDefinitionConstants.OPTION_MEDIA_TYPE_OUTPUT);
        IODescriptor readAs = generateIODescriptorForMediaType(outputType, ioDescriptor);

        FilesystemReadContext readContext = new FilesystemReadContext(runner.getHostWorkDir(), readAs.getName());
        Object value = varman.getValue(readAs.getPrimaryType(), readContext);
        P converted = (P)convertValue(camelContext, readAs, ioDescriptor, value);
        createMappedOutput(ioDescriptor.getName(), ioDescriptor.getPrimaryType(), converted, varman);
    }

}
