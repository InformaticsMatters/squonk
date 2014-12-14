package com.im.lac.portal.webapp;

import javax.swing.tree.DefaultMutableTreeNode;

public class VisualizerTreeNode extends DefaultMutableTreeNode {

    public VisualizerTreeNode(VisualizerTreeNodeData visualizerTreeNodeData) {
        super(visualizerTreeNodeData);
    }

    @Override
    public VisualizerTreeNodeData getUserObject() {
        return (VisualizerTreeNodeData) super.getUserObject();
    }

}
