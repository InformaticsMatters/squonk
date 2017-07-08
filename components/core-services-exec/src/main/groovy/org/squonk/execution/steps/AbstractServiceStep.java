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

import org.squonk.core.HttpServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.core.ServiceDescriptor;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.notebook.api.VariableKey;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 20/06/17.
 */
public abstract class AbstractServiceStep extends AbstractStep {

    private static final Logger LOG = Logger.getLogger(AbstractServiceStep.class.getName());

    protected ServiceDescriptor serviceDescriptor;


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

}
