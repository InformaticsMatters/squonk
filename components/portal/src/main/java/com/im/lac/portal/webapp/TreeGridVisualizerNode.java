package com.im.lac.portal.webapp;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeGridVisualizerNode extends DefaultMutableTreeNode {

    public TreeGridVisualizerNode(TreeGridVisualizerNodeData treeGridVisualizerNodeData) {
        super(treeGridVisualizerNodeData);
    }

    @Override
    public TreeGridVisualizerNodeData getUserObject() {
        return (TreeGridVisualizerNodeData) super.getUserObject();
    }

}
