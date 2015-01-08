package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.PropertyDescriptor;
import com.im.lac.portal.service.api.RowDescriptor;

import javax.swing.tree.DefaultMutableTreeNode;

public class DescriptorNodeData extends DefaultMutableTreeNode {

    private DatasetDescriptor datasetDescriptor;
    private RowDescriptor rowDescriptor;
    private PropertyDescriptor propertyDescriptor;
    private DescriptorType descriptorType;

    public String getDescription() {
        String description = null;
        if (descriptorType.equals(DescriptorType.DATASET)) {
            description = datasetDescriptor.getDescription();
        } else if (descriptorType.equals(DescriptorType.ROW)) {
            description = rowDescriptor.getDescription();
        } else if (descriptorType.equals(DescriptorType.PROPERTY)) {
            description = propertyDescriptor.getDescription();
        }
        return description;
    }

    public void setDatasetDescriptor(DatasetDescriptor datasetDescriptor) {
        this.datasetDescriptor = datasetDescriptor;
    }

    public void setRowDescriptor(RowDescriptor rowDescriptor) {
        this.rowDescriptor = rowDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public void setDescriptorType(DescriptorType descriptorType) {
        this.descriptorType = descriptorType;
    }


    protected enum DescriptorType {

        DATASET,
        ROW,
        PROPERTY

    }

}


