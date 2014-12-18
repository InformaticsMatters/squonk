package com.im.lac.portal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetRow {

    private Long id;
    private Map<String, Object> properties;
    private List<DatasetRow> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Deprecated
    public Map getProperties() {
        return properties;
    }

    @Deprecated
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        Object value = null;
        if (properties != null) {
            value = properties.get(key);
        }
        return value;
    }

    public DatasetRow createChild() {
        if (children == null) {
            children = new ArrayList<DatasetRow>();
        }
        DatasetRow datasetRow = new DatasetRow();
        children.add(datasetRow);
        return datasetRow;
    }

    public void removeChild(DatasetRow child) {
        if (children != null) {
            children.remove(child);
        }
    }
}
