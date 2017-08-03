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
import org.squonk.execution.runners.AbstractRunner;
import org.squonk.execution.variable.VariableManager;
import org.squonk.io.IODescriptor;

import java.util.logging.Logger;


/**
 * Handles thin Docker execution. The main work is done in the superclass with this class jsut overriding methods and calling
 * the 'Thin' equivalents.
 * <p>
 * <p>
 * Created by timbo on 23/02/17.
 */
public class ThinDatasetDockerExecutorStep extends DefaultDockerExecutorStep {

    private static final Logger LOG = Logger.getLogger(ThinDatasetDockerExecutorStep.class.getName());

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
