package org.squonk.core.service.discovery;

import org.squonk.core.ServiceDescriptor;

import java.util.*;

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
     * Get the endpoint, converting to absolute URL if needed
     *
     * @param serviceDescId
     * @return
     */
    public String resolveEndpoint(String serviceDescId) {
        Item item = items.get(serviceDescId);
        if (item == null) {
            return null;
        } else {
            return ServiceDescriptorUtils.makeAbsoluteUrl(item.baseUrl, item.serviceDescriptor);
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
