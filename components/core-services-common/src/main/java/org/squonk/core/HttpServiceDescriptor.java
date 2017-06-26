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

package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.io.Serializable;
import java.util.Date;

/**
 * Descriptor of some sort of service that can be executed. Currently there are two types:
 * <ol>
 * <li>REST (or plain HTTP) services</li>
 * <li>Docker containers</li>
 * </ol>
 * <p>
 * The basic process for REST services goes as follows:
 * <ol>
 * <li>The service implementation provides a URL that returns a List of ServiceDescriptors for
 * services it supports.</li>
 * <li>The administrator of the service registers this URL into the system to make the system aware
 * of the services</li>
 * <li>At runtime the system looks up the registered services, retrieves their ServiceDescriptors,
 * and makes those services available to the user</li>
 * <li>The user chooses to use a service. A UI is generated to allow them to define the appropriate
 * options for execution (see @{link #getOptionDescriptors()} for the defintion of the options.</li>
 * <li>When user chooses to submit the appropriate JobDefintion is created and POSTed to the job
 * service, using the executorClassName to orchestrate the process</li>
 * <li>A JobStatus is immediately returned that allows the job to be monitored and handled.</li>
 * </ol>
 * <p>
 * For Docker containers the process is similar, but the executionEndpoint property is used to define the default docker
 * image name to use, though executors may allow this to be overridden with one ot the options. The
 * orchestration is handled by the defined executorClassName.
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HttpServiceDescriptor extends DefaultServiceDescriptor implements Serializable {


    private final String executionEndpoint;

    @JsonCreator
    public HttpServiceDescriptor(
            @JsonProperty("serviceConfig") ServiceConfig serviceConfig,
            @JsonProperty("thinDescriptors") ThinDescriptor[] thinDescriptors,
            @JsonProperty("executionEndpoint") String executionEndpoint) {
        super(serviceConfig, thinDescriptors, null, null);
        this.executionEndpoint = executionEndpoint;
    }


    public HttpServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            ServiceConfig.Status status,
            Date statusLastChecked,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors,
            OptionDescriptor[] optionDescriptors,
            ThinDescriptor[] thinDescriptors,
            String executorClassName,
            String executionEndpoint) {

        super(id, name, description, tags, resourceUrl, icon, status, statusLastChecked,
                inputDescriptors, outputDescriptors, optionDescriptors,
                thinDescriptors, null, null, executorClassName);
        this.executionEndpoint = executionEndpoint;
    }

    public HttpServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            ServiceConfig.Status status,
            Date statusLastChecked,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors,
            OptionDescriptor[] optionDescriptors,
            String executorClassName,
            String executionEndpoint) {

        super(id, name, description, tags, resourceUrl, icon, status, statusLastChecked,
                inputDescriptors, outputDescriptors, optionDescriptors,
                null, null,null, executorClassName);
        this.executionEndpoint = executionEndpoint;
    }


    public HttpServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors,
            OptionDescriptor[] options,
            String executorClassName,
            String executionEndpoint) {
        this(id, name, description, tags, resourceUrl, icon, ServiceConfig.Status.UNKNOWN, null, inputDescriptors, outputDescriptors, options, executorClassName, executionEndpoint);
    }

    public HttpServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor[] inputDescriptors,
            IODescriptor[] outputDescriptors,
            OptionDescriptor[] options,
            ThinDescriptor[] thinDescriptors,
            String executorClassName,
            String executionEndpoint) {
        this(id, name, description, tags, resourceUrl, icon, ServiceConfig.Status.UNKNOWN, null, inputDescriptors, outputDescriptors, options, thinDescriptors, executorClassName, executionEndpoint);
    }

    public HttpServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor inputDescriptor,
            IODescriptor outputDescriptor,
            OptionDescriptor[] options,
            String executorClassName,
            String executionEndpoint) {
        this(id, name, description, tags, resourceUrl, icon,
                new IODescriptor[]{inputDescriptor}, new IODescriptor[]{outputDescriptor}, options, executorClassName, executionEndpoint);
    }


    public HttpServiceDescriptor(
            String id,
            String name,
            String description,
            String[] tags,
            String resourceUrl,
            String icon,
            IODescriptor inputDescriptor,
            IODescriptor outputDescriptor,
            OptionDescriptor[] options,
            ThinDescriptor thinDescriptor,
            String executorClassName,
            String executionEndpoint) {
        this(id, name, description, tags, resourceUrl, icon,
                new IODescriptor[]{inputDescriptor}, new IODescriptor[]{outputDescriptor}, options,
                thinDescriptor == null ? null : new ThinDescriptor[] {thinDescriptor},
                executorClassName, executionEndpoint);
    }


    public String getExecutionEndpoint() {
        return executionEndpoint;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("HttpServiceDescriptor[")
                .append("id:").append(getId())
                .append(" name:").append(serviceConfig.getName())
                .append(" executor:").append(serviceConfig.getExecutorClassName())
                .append(" endpoint:").append(executionEndpoint)
                .append("]");
        return b.toString();
    }


}
