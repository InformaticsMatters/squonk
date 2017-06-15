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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.io.Serializable;
import java.util.Date;

/** Defines the client side properties related to service execution. There properties defined the inputs, outputs, options
 * and cosmetics (name, description, icon etc.).
 * Execution specific details are not included here but will be defined in a @{link ExecutableServiceDescriptor} class from
 * which this ServiceConfig can be obtained.
 *
 * Created by timbo on 30/12/16.
 */
public class ServiceConfig implements Descriptor, Serializable{

    public enum Status {
        ACTIVE, INACTIVE, UNKNOWN
    }


    private final String id;
    private final String name;
    private final String description;
    private final String[] tags;
    private final String resourceUrl;
    private final String icon;
    private final IODescriptor[] inputDescriptors;
    private final IODescriptor[] outputDescriptors;
    private final OptionDescriptor[] optionDescriptors;
    private Status status;
    private Date statusLastChecked;
    private final String executorClassName;

    public ServiceConfig(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("tags") String[] tags,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("icon") String icon,
            @JsonProperty("inputDescriptors") IODescriptor[] inputDescriptors,
            @JsonProperty("outputDescriptors") IODescriptor[] outputDescriptors,
            @JsonProperty("optionDescriptors") OptionDescriptor[] optionDescriptors,
            @JsonProperty("status") ServiceConfig.Status status,
            @JsonProperty("statusLastChecked") Date statusLastChecked,
            @JsonProperty("executorClassName") String executorClassName
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.resourceUrl = resourceUrl;
        this.icon = icon;
        this.inputDescriptors = inputDescriptors;
        this.outputDescriptors = outputDescriptors;
        this.optionDescriptors = optionDescriptors;
        this.status = status;
        this.statusLastChecked = statusLastChecked;
        this.executorClassName = executorClassName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String[] getTags() {
        return tags;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public String getIcon() {
        return icon;
    }

    public IODescriptor[] getInputDescriptors() {
        return inputDescriptors;
    }

    public IODescriptor[] getOutputDescriptors() {
        return outputDescriptors;
    }

    public OptionDescriptor[] getOptionDescriptors() {
        return optionDescriptors;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getStatusLastChecked() {
        return statusLastChecked;
    }

    public void setStatusLastChecked(Date statusLastChecked) {
        this.statusLastChecked = statusLastChecked;
    }

    public String getExecutorClassName() {
        return executorClassName;
    }
}
