package com.im.lac.portal.webapp;

import com.inmethod.grid.IGridColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

public class VisualizerPanel extends Panel {

    public VisualizerPanel(String id) {
        super(id);
        addTreeTable();
    }

    private void addTreeTable() {
        add(new VisualizerTreeGrid("treeTable", createTreeGridModel(), createTreeGridColumns()));
    }

    private List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> createTreeGridColumns() {
        List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> columns;
        columns = new ArrayList<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>>();
        columns.add(new VisualizerTreeColumn("tree", Model.of("Tree")));
        columns.add(new VisualizerPropertyColumn("P1", "userObject.p1", "userObject.p1"));
        columns.add(new VisualizerPropertyColumn("P2", "userObject.p2", "userObject.p2"));
        columns.add(new VisualizerPropertyColumn("P3", "userObject.p3", "userObject.p3"));
        return columns;
    }

    private VisualizerTreeModel createTreeGridModel() {
        VisualizerTreeNode rootNode = new VisualizerTreeNode(new VisualizerTreeNodeData("root", "", ""));

        VisualizerTreeNode rootChild1Node = new VisualizerTreeNode(new VisualizerTreeNodeData("S1 P1", "S1 P2", "S1 P3"));
        rootNode.add(rootChild1Node);
        rootChild1Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("S1 B1 P1", "S1 B1 P2", "S1 B1 P3")));
        rootChild1Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("S1 B2 P1", "S1 B2 P2", "S1 B2 P3")));

        VisualizerTreeNode rootChild2Node = new VisualizerTreeNode(new VisualizerTreeNodeData("S2 P1", "S2 P2", "S2 P3"));
        rootNode.add(rootChild2Node);

        rootChild2Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("S2 B1 P1", "S2 B1 P2", "S2 B1 P3")));
        rootChild2Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("S2 B2 P1", "S2 B2 P2", "S2 B2 P3")));

        return new VisualizerTreeModel(rootNode);
    }

}
