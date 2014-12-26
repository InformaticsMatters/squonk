package com.im.lac.portal.service.mock;

import com.im.lac.portal.service.api.PropertyDescriptor;
import com.im.lac.portal.service.api.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RowDescriptorMock implements RowDescriptor {

    private Long id;
    private String description;
    private Map<Long, PropertyDescriptorMock> propertyDescriptorMap = new HashMap<Long, PropertyDescriptorMock>();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<PropertyDescriptor> listAllPropertyDescriptors() {
        return new ArrayList<PropertyDescriptor>(propertyDescriptorMap.values());
    }

    @Override
    public PropertyDescriptor findPropertyDescriptorById(Long id) {
        return propertyDescriptorMap.get(id);
    }

    public void addPropertyDescriptor(PropertyDescriptorMock propertyDescriptor) {
        propertyDescriptorMap.put(propertyDescriptor.getId(), propertyDescriptor);
    }

    public void removePropertyDescriptor(Long id) {
        propertyDescriptorMap.remove(id);
    }

}
