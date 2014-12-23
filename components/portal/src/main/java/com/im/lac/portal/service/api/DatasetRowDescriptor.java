package com.im.lac.portal.service.api;

import java.util.ArrayList;
import java.util.List;

public class DatasetRowDescriptor {

    private Long id;
    private String description;
    private List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();

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

    public void addPropertyDefinition(PropertyDescriptor propertyDescriptor) {
        propertyDescriptorList.add(propertyDescriptor);
    }

    public void removePropertyDefinition(PropertyDescriptor propertyDescriptor) {
        propertyDescriptorList.remove(propertyDescriptor);
    }

    public List<PropertyDescriptor> getPropertyDescriptorList() {
        return propertyDescriptorList;
    }

}
