package org.squonk.core.service.discovery;

import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author timbo
 */
public class ServiceDescriptorStore {

    private final Map<String,ServiceDescriptorSet> items = new ConcurrentHashMap<>();

    public List<ServiceDescriptor> getServiceDescriptors() {
        List<ServiceDescriptor> list = new ArrayList<>();
        for (ServiceDescriptorSet item : items.values()) {
            list.addAll(item.getServiceDescriptors());
        }
        return list;
    }

    public List<ServiceDescriptorSet> getServiceDescriptorSets() {
        List<ServiceDescriptorSet> list = new ArrayList<>();
        list.addAll(items.values());
        return list;
    }

    public void updateServiceDescriptors(String baseUrl, String healthUrl, List<ServiceDescriptor> serviceDescriptors) {
        ServiceDescriptorSet sdset = items.get(baseUrl);
        if (sdset == null) {
            items.put(baseUrl, new ServiceDescriptorSet(baseUrl, healthUrl, serviceDescriptors));
        } else {
            sdset.updateServiceDescriptors(serviceDescriptors);
        }
    }


    public void removeServiceDescriptors(String baseUrl) {
        items.remove(baseUrl);
    }


}
