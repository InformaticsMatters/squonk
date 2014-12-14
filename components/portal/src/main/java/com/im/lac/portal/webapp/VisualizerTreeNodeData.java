package com.im.lac.portal.webapp;

import java.io.Serializable;

class VisualizerTreeNodeData implements Serializable {

    private String description;

    VisualizerTreeNodeData(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
