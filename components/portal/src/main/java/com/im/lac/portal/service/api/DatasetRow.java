package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.*;

public class DatasetRow implements Serializable {

    private Long id;
    private Map<String, Object> properties;
    private List<DatasetRow> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<String> getPropertyKeys() {
        return properties.keySet();
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

    public List<DatasetRow> getChildren() {
        return children;
    }
}
