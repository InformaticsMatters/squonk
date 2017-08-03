package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.execution.runners.AbstractRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;

/**
 * Created by timbo on 03/08/17.
 */
public class ThinDatasetNextflowExecutorStep extends DatasetNextflowExecutorStep {

    @Override
    protected void handleInputs(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            AbstractRunner runner) throws Exception {
        handleThinInputs(camelContext, serviceDescriptor, varman, runner);
    }

    @Override
    protected <P, Q> void handleInput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            AbstractRunner runner,
            IODescriptor<P, Q> ioDescriptor) throws Exception {

        handleThinInput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
    }

    @Override
    protected <P, Q> void handleOutput(
            CamelContext camelContext,
            DefaultServiceDescriptor serviceDescriptor,
            VariableManager varman,
            AbstractRunner runner,
            IODescriptor<P, Q> ioDescriptor) throws Exception {

        handleThinOutput(camelContext, serviceDescriptor, varman, runner, ioDescriptor);
    }
}
