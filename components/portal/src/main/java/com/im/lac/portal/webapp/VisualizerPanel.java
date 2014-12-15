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
        VisualizerTreeModel treeGridModel = createTreeGridModel();
        List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> columns = createTreeTableColumns();
        VisualizerTreeGrid treeGrid = new VisualizerTreeGrid("treeTable", treeGridModel, columns);
        add(treeGrid);
    }

    private List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> createTreeTableColumns() {
        List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> columns;
        columns = new ArrayList<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>>();
        columns.add(new VisualizerTreeColumn("tree", Model.of("Tree")));
        columns.add(new VisualizerPropertyColumn("P1", "userObject.description", "userObject.description"));
        return columns;
    }

    private VisualizerTreeModel createTreeGridModel() {
        VisualizerTreeNode rootNode = new VisualizerTreeNode(new VisualizerTreeNodeData("root"));
        VisualizerTreeModel model = new VisualizerTreeModel(rootNode);

        VisualizerTreeNode rootChild1Node = new VisualizerTreeNode(new VisualizerTreeNodeData("Structure1"));
        rootNode.add(rootChild1Node);
        rootChild1Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("child1 of Structure1")));
        rootChild1Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("child2 of Structure1")));

        VisualizerTreeNode rootChild2Node = new VisualizerTreeNode(new VisualizerTreeNodeData("Structure2"));
        rootNode.add(rootChild2Node);

        rootChild2Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("child1 of Structure2")));
        rootChild2Node.add(new VisualizerTreeNode(new VisualizerTreeNodeData("child2 of Structure2")));

        return model;
    }

}
