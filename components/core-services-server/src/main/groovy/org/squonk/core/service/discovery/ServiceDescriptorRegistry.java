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

package org.squonk.core.service.discovery;

import org.squonk.core.DockerServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.execution.steps.impl.GenericDatasetDockerExecutorStep;
import org.squonk.io.IODescriptors;
import org.squonk.io.IORoute;
import org.squonk.options.MultiLineTextTypeDescriptor;
import org.squonk.options.OptionDescriptor;
import org.squonk.util.CommonMimeTypes;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 05/12/16.
 */
public class ServiceDescriptorRegistry {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorRegistry.class.getName());

    private final PostgresServiceDescriptorClient client;

    private final Map<String, ServiceDescriptorSet> serviceDescriptorSets = new LinkedHashMap<>();
    private final Map<String, ServiceDescriptor> serviceDescriptors = new LinkedHashMap<>();
    private final Object writeLock = new Object();
    private boolean initialised = false;

    private static ServiceDescriptorSet BUILT_IN_SERVICES = new StandardServiceDescriptors().get();

    public ServiceDescriptorRegistry() {
        this.client = null;
    }

    public ServiceDescriptorRegistry(PostgresServiceDescriptorClient client) {
        this.client = client;
    }

    /**
     * Initialise the registry by loading the saved descriptors
     */
    protected void init() throws Exception {
        if (!initialised) {
            load();
            initialised = true;
        }
    }

    private void load() throws Exception {
        if (client != null) {
            List<ServiceDescriptorSet> sets = client.list();
            sets.forEach(set -> {
                serviceDescriptorSets.put(set.getBaseUrl(), set);
                set.getServiceDescriptors().forEach(sd -> serviceDescriptors.put(sd.getId(), sd));
            });
        }
    }

    public List<ServiceDescriptorSet> fetchServiceDescriptorSets() {
        List<ServiceDescriptorSet> list = serviceDescriptorSets.values().stream().collect(Collectors.toList());
        list.add(BUILT_IN_SERVICES);
        return list;
    }

    public List<ServiceDescriptor> fetchServiceDescriptors() {
        List<ServiceDescriptor> list = serviceDescriptors.values().stream().collect(Collectors.toList());
        list.addAll(BUILT_IN_SERVICES.getServiceDescriptors());
        return list;
    }

    public List<ServiceConfig> fetchServiceConfigs() {
        return fetchServiceDescriptors().stream().map(sd -> sd.getServiceConfig()).collect(Collectors.toList());
    }


    public ServiceDescriptor fetchServiceDescriptor(String id) {
        ServiceDescriptor sd = BUILT_IN_SERVICES.findServiceDescriptor(id);
        if (sd == null) {
            sd = serviceDescriptors.get(id);
        }
        return sd;
    }

    /**
     * Fetch the ServiceDescriptorSet associated with this url, creating it if it doesn't already exist.
     * You can make changes to this set, but to make sure the changes are persisted you must call the
     *
     * @param baseUrl
     * @return
     * @{link updateServiceDescriptorSet} method.
     */
    public ServiceDescriptorSet fetchServiceDescriptorSet(String baseUrl) {
        if (baseUrl == null) {
            return BUILT_IN_SERVICES;
        }
        synchronized (writeLock) {
            ServiceDescriptorSet set = serviceDescriptorSets.get(baseUrl);
            if (set == null) {
                set = new ServiceDescriptorSet(baseUrl, null);
                serviceDescriptorSets.put(baseUrl, set);
            }
            return set;
        }
    }

    /**
     * Update details for this set AND  persist the changes.
     *
     * @param set
     */
    public void updateServiceDescriptorSet(ServiceDescriptorSet set) {
        LOG.fine("Updating service descriptors for " + set.getBaseUrl());
        if (set.getBaseUrl() == null) {
            return;
        }
        synchronized (writeLock) {
            ServiceDescriptorSet old = serviceDescriptorSets.get(set.getBaseUrl());
            if (old != null) {
                // delete the old
                old.getServiceDescriptors().forEach(sd -> serviceDescriptors.remove(sd.getId()));
            }
            // add the new
            set.getServiceDescriptors().forEach(sd -> serviceDescriptors.put(sd.getId(), sd));
            serviceDescriptorSets.put(set.getBaseUrl(), set);
            saveServiceDescriptorSet(set);
        }
    }


    /**
     * Persist this set to the database
     *
     * @param set
     */
    private void saveServiceDescriptorSet(ServiceDescriptorSet set) {
        if (client != null && set.getBaseUrl() != null) {
            try {
                client.update(set);
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Failed to save ServiceDescriptorSet " + set.getBaseUrl(), e);
            }
        }
    }

}
