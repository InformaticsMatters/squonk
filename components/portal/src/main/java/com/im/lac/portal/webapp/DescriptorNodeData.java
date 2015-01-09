package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.PropertyDescriptor;
import com.im.lac.portal.service.api.RowDescriptor;

import java.io.Serializable;

public class DescriptorNodeData implements Serializable {

    private Object descriptor;

    public DescriptorNodeData(Object descriptor) {
        this.descriptor = descriptor;
    }

    public String getDescription() {
        String result = null;

        if (descriptor != null) {
            if (descriptor instanceof DatasetDescriptor) {
                result = ((DatasetDescriptor) descriptor).getDescription();
            } else if (descriptor instanceof RowDescriptor) {
                result = ((RowDescriptor) descriptor).getDescription();
            } else if (descriptor instanceof PropertyDescriptor) {
                result = ((PropertyDescriptor) descriptor).getDescription();
            }
        }
        return result;
    }
}


