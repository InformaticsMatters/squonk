package com.im.lac.portal.webapp;

import java.io.Serializable;

class VisualizerTreeNodeData implements Serializable {

    private String p3;
    private String p2;
    private String p1;

    VisualizerTreeNodeData(String p1, String p2, String p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getP3() {
        return p3;
    }

    public void setP3(String p3) {
        this.p3 = p3;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }
}
