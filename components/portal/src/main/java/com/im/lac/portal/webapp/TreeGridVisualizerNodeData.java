package com.im.lac.portal.webapp;

import com.im.lac.portal.service.DatasetRow;

import java.io.Serializable;

class TreeGridVisualizerNodeData implements Serializable {

    private DatasetRow datasetRow;
    private String dummy = "foo";

    TreeGridVisualizerNodeData(DatasetRow datasetRow) {
        this.datasetRow = datasetRow;
    }

    public Long getId() {
        return datasetRow.getId();
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
