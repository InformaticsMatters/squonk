package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DatasetRowDescriptor implements Serializable {

    private Long id;
    private String description;
    private Map<Long, PropertyDescriptor> propertyDescriptorMap = new HashMap<Long, PropertyDescriptor>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        propertyDescriptorMap.put(propertyDescriptor.getId(), propertyDescriptor);
    }

    public void removePropertyDescriptor(Long id) {
        propertyDescriptorMap.remove(id);
    }

    public PropertyDescriptor getPropertyDescriptor(Long id){
        return propertyDescriptorMap.get(id);
    }

    public Collection<Long> getPropertyDescriptorKeys() {
        return propertyDescriptorMap.keySet();
    }

}
