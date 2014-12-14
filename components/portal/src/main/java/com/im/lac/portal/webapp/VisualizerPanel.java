package com.im.lac.portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.column.PropertyColumn;
import com.inmethod.grid.treegrid.TreeGrid;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public class VisualizerPanel extends Panel {

    public VisualizerPanel(String id) {
        super(id);
        addTreeTable();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
    }

    private void addTreeTable() {
        DefaultTreeModel treeGridModel = createTreeGridModel();
        List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> columns = createTreeTableColumns();
        TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String> treeGrid = new TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String>("treeTable", treeGridModel, columns);
        add(treeGrid);
    }

    private List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> createTreeTableColumns() {
        List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> columns;
        columns = new ArrayList<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>>();
        columns.add(createTreeColumn());
        columns.add(new PropertyColumn<DefaultTreeModel, DefaultMutableTreeNode, String, String>("p1", Model.of("P1"), "userObject.description", "userObject.description"));
        return columns;
    }

    private VisualizerTreeColumn createTreeColumn() {
        return new VisualizerTreeColumn("tree", Model.of("Tree"));
    }

    private DefaultTreeModel createTreeGridModel() {
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
