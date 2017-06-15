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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.dataset.ThinDescriptor;
import org.squonk.io.IODescriptor;
import org.squonk.options.OptionDescriptor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by timbo on 04/01/17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DefaultServiceDescriptor implements ServiceDescriptor, Serializable {

    final ServiceConfig serviceConfig;
    final ThinDescriptor[] thinDescriptors;




    public DefaultServiceDescriptor(
            @JsonProperty("serviceConfig") ServiceConfig serviceConfig,
            @JsonProperty("thinDescriptors") ThinDescriptor[] thinDescriptors) {
        this.serviceConfig = serviceConfig;
        this.thinDescriptors = thinDescriptors;
    }


    public DefaultServiceDescriptor(
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
            String executorClassName) {

        this(new ServiceConfig(id, name, description, tags, resourceUrl, icon,
                inputDescriptors, outputDescriptors, optionDescriptors, status, statusLastChecked, executorClassName),
                thinDescriptors);


    }

    @JsonIgnore
    @Override
    public String getId() {
        return serviceConfig.getId();
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }


    public ThinDescriptor[] getThinDescriptors() {
        return thinDescriptors;
    }
}
