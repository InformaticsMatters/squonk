package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.Row;

import java.io.Serializable;

class TreeGridVisualizerNodeData implements Serializable {

    private Row datasetRow;

    TreeGridVisualizerNodeData(Row datasetRow) {
        this.datasetRow = datasetRow;
    }

    public TreeGridVisualizerNodeData() {
    }

    public Long getId() {
        return datasetRow.getId();
    }

    public Object getPropertyValue(Long propertyId) {
        Object result = null;
        if (datasetRow != null) {
            return datasetRow.getProperty(datasetRow.getDescriptor().findPropertyDescriptorById(propertyId));
        }
        return result;
    }

}
