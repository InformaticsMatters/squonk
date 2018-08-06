package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.core.NextflowServiceDescriptor;
import org.squonk.execution.runners.ContainerRunner;
import org.squonk.execution.runners.DockerRunner;
import org.squonk.execution.steps.AbstractServiceStep;
import org.squonk.execution.variable.VariableManager;
import org.squonk.util.IOUtils;

import java.util.Properties;
import java.util.logging.Logger;

/** Step that executes a <a href="http://nextflow.io">Nextflow</a> workflow, executing it inside a Docker container.
 *
 * IMPORTANT: Details of this step are subject to change.
 *
 * Created by timbo on 28/07/17.
 */
public class DatasetNextflowInDockerExecutorStep extends AbstractServiceStep {

    @Override
    public void execute(VariableManager varman, CamelContext context) throws Exception {
        throw new IllegalStateException("NYI");
    }

    protected void doExecute(VariableManager varman, CamelContext camelContext, NextflowServiceDescriptor descriptor) throws Exception {
        throw new IllegalStateException("NYI");
    }

}
