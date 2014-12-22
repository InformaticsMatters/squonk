package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetRow;

import java.io.Serializable;

class TreeGridVisualizerNodeData implements Serializable {

    private DatasetRow datasetRow;

    TreeGridVisualizerNodeData(DatasetRow datasetRow) {
        this.datasetRow = datasetRow;
    }

    public Long getId() {
        return datasetRow.getId();
    }

    public Object getPropertyValue(String propertyName) {
        return datasetRow.getProperty(propertyName);
    }

}
