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
import org.squonk.io.IORoute;
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
    final IORoute[] inputRoutes;
    final IORoute[] outputRoutes;

    public DefaultServiceDescriptor(
            @JsonProperty("serviceConfig") ServiceConfig serviceConfig,
            @JsonProperty("thinDescriptors") ThinDescriptor[] thinDescriptors,
            @JsonProperty("inputRoutes") IORoute[] inputRoutes,
            @JsonProperty("outputRoutes") IORoute[] outputRoutes) {
        this.serviceConfig = serviceConfig;
        this.inputRoutes = inputRoutes;
        this.outputRoutes = outputRoutes;
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
            IORoute[] inputRoutes,
            IORoute[] outputRoutes,
            String executorClassName) {

        this(new ServiceConfig(id, name, description, tags, resourceUrl, icon,
                inputDescriptors, outputDescriptors, optionDescriptors, status, statusLastChecked, executorClassName),
                thinDescriptors, inputRoutes, outputRoutes);
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

    @Override
    public IORoute[] getInputRoutes() {
        return inputRoutes;
    }

    @Override
    public IORoute[] getOutputRoutes() {
        return outputRoutes;
    }

    /** Resolve the input IODescriptors that are to be used at runtime. If getInputRoutes() is not null these they are used.
     * If not, or one of the IORoutes is null, or its IODescriptor is null then fallback to the input IODescriptors from
     * the serviceConfig are used.
     * Implicit in the logic is that the IODescriptor for the serviceConfig MUST be defined, and MUST NOT be null.
     *
     * @return
     */
    public IODescriptor[] resolveInputIODescriptors() {
        return resolveIODescriptors(serviceConfig.getInputDescriptors(), getInputRoutes());
    }

    /** Resolve the output IODescriptors that are to be used at runtime. Logic is similar to that used for resolveInputIODescriptors()
     *
     * @return
     */
    public IODescriptor[] resolveOutputIODescriptors() {
        return resolveIODescriptors(serviceConfig.getOutputDescriptors(), getOutputRoutes());
    }

    private IODescriptor[] resolveIODescriptors(IODescriptor[] serviceInputs, IORoute[] routes) {
        if (routes == null) {
            return serviceInputs;
        } else {
            IODescriptor[] results = new IODescriptor[serviceInputs.length];
            for (int i=0; i<serviceInputs.length; i++) {
                if (routes.length > i && routes[i] != null && routes[i].getDescriptor() != null) {
                    results[i] = routes[i].getDescriptor();
                } else {
                    results[i] = serviceInputs[i];
                }
            }
            return results;
        }
    }
}
