package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.Row;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeGridVisualizerNode extends DefaultMutableTreeNode {

    @Override
    public Row getUserObject() {
        return (Row) super.getUserObject();
    }
}
