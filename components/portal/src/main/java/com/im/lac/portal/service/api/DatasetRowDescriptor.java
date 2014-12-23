package com.im.lac.portal.service.api;

import java.util.ArrayList;
import java.util.List;

public class DatasetRowDescriptor {

    private Long id;
    private String description;
    private List<PropertyDefinition> propertyDefinitionList = new ArrayList<PropertyDefinition>();

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

    public void addPropertyDefinition(PropertyDefinition propertyDefinition) {
        if (propertyDefinitionList == null) {
            propertyDefinitionList = new ArrayList<PropertyDefinition>();
        }
        propertyDefinitionList.add(propertyDefinition);
    }

    public void removePropertyDefinition(PropertyDefinition propertyDefinition) {
        if (propertyDefinitionList != null) {
            propertyDefinitionList.remove(propertyDefinition);
        }
    }
}
