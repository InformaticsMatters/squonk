package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.List;

public class DatasetDescriptor implements Serializable {

    private Long datasetId;
    private String description;
    private List<DatasetRowDescriptor> datasetRowDescriptorList;

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
