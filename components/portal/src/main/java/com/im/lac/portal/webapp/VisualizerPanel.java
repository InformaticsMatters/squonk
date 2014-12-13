package com.im.lac.portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.treegrid.TreeGrid;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.Serializable;
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
        return columns;
    }

    private StructureTreeColumn createTreeColumn() {
        return new StructureTreeColumn("tree", Model.of("Tree"));
    }

    private DefaultTreeModel createTreeGridModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeColumnObject("root"));
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        DefaultMutableTreeNode rootChild1Node = new DefaultMutableTreeNode(new TreeColumnObject("Structure1"));
        rootNode.add(rootChild1Node);
        DefaultMutableTreeNode rootChild2Node = new DefaultMutableTreeNode(new TreeColumnObject("Structure2"));
        rootNode.add(rootChild2Node);

        rootChild1Node.add(new DefaultMutableTreeNode(new TreeColumnObject("child1 of Structure1")));
        rootChild1Node.add(new DefaultMutableTreeNode(new TreeColumnObject("child2 of Structure1")));

        rootChild2Node.add(new DefaultMutableTreeNode(new TreeColumnObject("child1 of Structure2")));
        rootChild2Node.add(new DefaultMutableTreeNode(new TreeColumnObject("child2 of Structure2")));

        return model;
    }

    private class TreeColumnObject implements Serializable {

        private String description;

        private TreeColumnObject(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
