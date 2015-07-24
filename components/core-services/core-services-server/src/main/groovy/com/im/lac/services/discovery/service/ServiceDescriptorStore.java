package com.im.lac.services.discovery.service;

import com.im.lac.services.AccessMode;
import com.im.lac.services.ServiceDescriptor;
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
