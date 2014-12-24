package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DatasetDescriptor implements Serializable {

    private Long id;
    private String description;
    private Map<Long, DatasetRowDescriptor> datasetRowDescriptorMap = new HashMap<Long, DatasetRowDescriptor>();

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

    public void addDatasetRowDescriptor(DatasetRowDescriptor datasetRowDescriptor) {
        datasetRowDescriptorMap.put(datasetRowDescriptor.getId(), datasetRowDescriptor);
    }

    public void removeDatasetRowDescriptor(Long id) {
        datasetRowDescriptorMap.remove(id);
    }

    public DatasetRowDescriptor getDatasetRowDescriptor(Long id) {
        return datasetRowDescriptorMap.get(id);
    }

    public Collection<Long> getDatasetRowDescriptorKeys() {
        return datasetRowDescriptorMap.keySet();
    }
}
