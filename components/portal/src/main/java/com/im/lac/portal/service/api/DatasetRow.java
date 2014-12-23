package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.*;

public class DatasetRow implements Serializable {

    private Long id;
    private DatasetRowDescriptor datasetRowDescriptor;
    private Map<PropertyDescriptor, Object> properties;
    private List<DatasetRow> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DatasetRowDescriptor getDatasetRowDescriptor() {
        return datasetRowDescriptor;
    }

    public void setDatasetRowDescriptor(DatasetRowDescriptor datasetRowDescriptor) {
        this.datasetRowDescriptor = datasetRowDescriptor;
    }

    public Set<PropertyDescriptor> getPropertyKeys() {
        return properties.keySet();
    }

    public void setProperty(PropertyDescriptor key, Object value) {
        if (properties == null) {
            properties = new HashMap<PropertyDescriptor, Object>();
        }
        properties.put(key, value);
    }

    public Object getProperty(PropertyDescriptor key) {
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
