package com.im.lac.portal.webapp;

import com.im.lac.portal.service.DatasetRow;

import java.io.Serializable;

class VisualizerTreeNodeData implements Serializable {

    private DatasetRow datasetRow;
    private String dummy = "foo";

    VisualizerTreeNodeData(DatasetRow datasetRow) {
        this.datasetRow = datasetRow;
    }

    public Object getPropertyValue(String propertyName) {
        return datasetRow.getProperty(propertyName);
    }

    public String getDummy() {
        return dummy;
    }

    public void setDummy(String dummy) {
        this.dummy = dummy;
    }
}
