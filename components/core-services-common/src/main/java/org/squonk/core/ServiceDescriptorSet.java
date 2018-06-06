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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.*;

/**
 * Definition of set of ServiceDescriptors. Each set is defined at a specific URL, with the
 * individual ServiceDescriptor endpoint possibly being relative to this base URL.
 *
 *
 * @author timbo
 */
public class ServiceDescriptorSet implements Serializable {

    private final String baseUrl;
    private String healthUrl;

    @JsonIgnore
    private final Map<String,ServiceDescriptor> serviceDescriptorsMap = new LinkedHashMap<>();

    public ServiceDescriptorSet(
            @JsonProperty("baseUrl") String baseUrl,
            @JsonProperty("healthUrl") String healthUrl,
            @JsonProperty("serviceDescriptors") List<ServiceDescriptor> serviceDescriptors) {
        this.baseUrl = baseUrl;
        this.healthUrl = healthUrl;
        if (serviceDescriptors != null) {
            serviceDescriptors.forEach(sd -> {
                serviceDescriptorsMap.put(sd.getId(), sd);
            });
        }
    }

    public ServiceDescriptorSet(String baseUrl, String healthUrl) {
        this.baseUrl = baseUrl;
        this.healthUrl = healthUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /** Get the absolute URL that can be used to test if these services are running healthily.
     * If a GET operation on this URL gives a 200 response then all services are considered healthy.
     *
     * @return
     */
    public String getHealthUrl() {
        return healthUrl;
    }

    public void setHealthUrl(String healthUrl) {
        this.healthUrl = healthUrl;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public List<ServiceDescriptor> getServiceDescriptors() {
        List<ServiceDescriptor> list = new ArrayList<>();
        list.addAll(serviceDescriptorsMap.values());
        return list;
    }

    public ServiceDescriptor findServiceDescriptor(String id) {
        return serviceDescriptorsMap.get(id);
    }

    @JsonIgnore
    public List<ServiceConfig> getAsServiceConfigs() {
        final List<ServiceConfig> list = new ArrayList<>();
        getServiceDescriptors().forEach(h -> list.add(h.getServiceConfig()));
        return list;
    }

    public void updateServiceDescriptor(ServiceDescriptor sd) {
        serviceDescriptorsMap.put(sd.getId(), sd);
    }

    public void updateServiceDescriptors(List<? extends ServiceDescriptor> sds) {
        sds.forEach(sd -> serviceDescriptorsMap.put(sd.getId(), sd));
    }

    public void removeServiceDescriptor(String id) {
        serviceDescriptorsMap.remove(id);
    }

}
