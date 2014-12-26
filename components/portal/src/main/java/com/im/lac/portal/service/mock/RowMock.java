package com.im.lac.portal.service.mock;

import com.im.lac.portal.service.api.PropertyDescriptor;
import com.im.lac.portal.service.api.Row;
import com.im.lac.portal.service.api.RowDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RowMock implements Row {

    private Long id;
    private RowDescriptorMock rowDescriptor;
    private Map<PropertyDescriptorMock, Object> properties;
    private List<Row> children;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public RowDescriptor getDescriptor() {
        return rowDescriptor;
    }

    @Override
    public List<Row> getChildren() {
        return children;
    }

    public void setRowDescriptor(RowDescriptorMock rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    public void setProperty(PropertyDescriptorMock key, Object value) {
        if (properties == null) {
            properties = new HashMap<PropertyDescriptorMock, Object>();
        }
        properties.put(key, value);
    }

    public Object getProperty(PropertyDescriptor propertyDescriptor) {
        Object value = null;
        if (properties != null) {
            value = properties.get(propertyDescriptor);
        }
        return value;
    }

    public RowMock createChild() {
        if (children == null) {
            children = new ArrayList<Row>();
        }
        RowMock rowMock = new RowMock();
        children.add(rowMock);
        return rowMock;
    }

    public void removeChild(RowMock child) {
        if (children != null) {
            children.remove(child);
        }
    }

}
