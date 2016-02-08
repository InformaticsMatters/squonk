package org.squonk.core.discovery.service;

import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class ServiceDescriptorStore {

    private final Map<String, Item> items = Collections.synchronizedMap(new HashMap<>());

    public List<ServiceDescriptor> getServiceDescriptors() {
        List<ServiceDescriptor> list = new ArrayList<>();
        for (Item item : items.values()) {
            list.add(item.serviceDescriptor);
        }
        return list;
    }

    /**
     * Resolve the endpoint, converting to an absolute URL if necessary
     *
     * @param serviceDescId
     * @param accessModeId
     * @return
     */
    public String resolveEndpoint(String serviceDescId, String accessModeId) {
        Item item = items.get(serviceDescId);
        if (item == null) {
            return null;
        } else {
            for (AccessMode mode : item.serviceDescriptor.getAccessModes()) {
                if (accessModeId.equals(mode.getId())) {
                    return ServiceDescriptorUtils.getAbsoluteUrl(item.baseUrl, mode);
                }
            }
        }
        return null;
    }

    /**
     * Resolve the endpoint, converting to an absolute URL if necessary
     *
     * @param serviceDescId
     * @param accessMode
     * @return
     */
    public String resolveEndpoint(String serviceDescId, AccessMode accessMode) {
        Item item = items.get(serviceDescId);
        if (item == null) {
            return null;
        } else {
            return ServiceDescriptorUtils.getAbsoluteUrl(item.baseUrl, accessMode);
        }
    }

    /**
     * Get the endpoint as it is, assuming it is an absolute URL
     *
     * @param serviceDescId
     * @param accessModeId
     * @return
     */
    public String getEndpoint(String serviceDescId, String accessModeId) {
        Item item = items.get(serviceDescId);
        if (item == null) {
            return null;
        } else {
            AccessMode mode = ServiceDescriptorUtils.findAccessMode(item.serviceDescriptor, accessModeId);
            if (mode == null) {
                return null;
            }
            if (mode.isEndpointRelative()) {
                throw new IllegalStateException("Endpoint is relative - must be resolved agaisnt a base URL");
            }
            return mode.getExecutionEndpoint();
        }
    }

    public void addServiceDescriptors(String baseUrl, ServiceDescriptor[] serviceDescriptors) {
        for (ServiceDescriptor serviceDescriptor : serviceDescriptors) {
            addServiceDescriptor(baseUrl, serviceDescriptor);
        }
    }

    public void addServiceDescriptor(String baseUrl, ServiceDescriptor serviceDescriptor) {
        Item item = new Item(baseUrl, serviceDescriptor);
        items.put(serviceDescriptor.getId(), item);
    }

    public void removeServiceDescriptors(String baseUrl) {
        Iterator<Map.Entry<String, Item>> it = items.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Item> e = it.next();
            if (baseUrl.equals(e.getValue().baseUrl)) {
                it.remove();
            }
        }
    }

    public ServiceDescriptor getServiceDescriptor(String id) {
        Item item = items.get(id);
        if (item == null) {
            return null;
        } else {
            return item.serviceDescriptor;

        }
    }

    class Item {

        final String baseUrl;
        final ServiceDescriptor serviceDescriptor;

        Item(String baseUrl, ServiceDescriptor serviceDescriptor) {
            this.baseUrl = baseUrl;
            this.serviceDescriptor = serviceDescriptor;
        }
    }

}
