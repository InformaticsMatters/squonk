package com.im.lac.portal.service;

import java.util.Map;

public class DatasetRow {

    private Long id;
    private Map properties;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }
}
