package org.squonk.core.service.discovery;

import org.squonk.core.ServiceConfig;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 05/12/16.
 */
public class ServiceDescriptorRegistry {

    private static final Logger LOG = Logger.getLogger(ServiceDescriptorRegistry.class.getName());

    private final PostgresServiceDescriptorClient client;

    private final Map<String,ServiceDescriptorSet> serviceDescriptorSets = new LinkedHashMap<>();
    private final Map<String,ServiceDescriptor> serviceDescriptors = new LinkedHashMap<>();
    private final Object writeLock = new Object();
    private boolean initialised = false;

    public ServiceDescriptorRegistry() {
        this.client = null;
    }

    public ServiceDescriptorRegistry(PostgresServiceDescriptorClient client) {
        this.client = client;
    }

    /** Initialise the registry by loading the saved descriptors
     *
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
        return serviceDescriptorSets.values().stream().collect(Collectors.toList());
    }

    public List<ServiceDescriptor> fetchServiceDescriptors() {
        return serviceDescriptors.values().stream().collect(Collectors.toList());
    }

    public List<ServiceConfig> fetchServiceConfigs() {
        return serviceDescriptors.values().stream().map(sd -> sd.getServiceConfig()).collect(Collectors.toList());
    }


    public ServiceDescriptor fetchServiceDescriptor(String id) {
        return serviceDescriptors.get(id);
    }

    /** Fetch the ServiceDescriptorSet associated with this url, creating it if it doesn't already exist.
     * You can make changes to this set, but to make sure the changes are persisted you must call the
     * @{link updateServiceDescriptorSet} method.
     *
     * @param baseUrl
     * @return
     */
    public ServiceDescriptorSet fetchServiceDescriptorSet(String baseUrl) {
        synchronized (writeLock) {
            ServiceDescriptorSet set = serviceDescriptorSets.get(baseUrl);
            if (set == null) {
                set = new ServiceDescriptorSet(baseUrl, null);
                serviceDescriptorSets.put(baseUrl, set);
            }
            return set;
        }
    }

    /** Update details for this set AND  persist the changes.
     *
     * @param set
     */
    public void updateServiceDescriptorSet(ServiceDescriptorSet set) {
        LOG.fine("Updating service descriptors for " + set.getBaseUrl());
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


    /** Persist this set to the database
     *
     * @param set
     */
    private void saveServiceDescriptorSet(ServiceDescriptorSet set) {
        if (client != null) {
            try {
                client.update(set);
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Failed to save ServiceDescriptorSet " + set.getBaseUrl(), e);
            }
        }
    }

}
