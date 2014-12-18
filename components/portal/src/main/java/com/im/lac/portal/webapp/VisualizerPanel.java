package com.im.lac.portal.webapp;

import com.im.lac.portal.service.DatasetRow;
import com.inmethod.grid.IGridColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

public class VisualizerPanel extends Panel {

    private VisualizerTreeGrid visualizerTreeGrid;

    public VisualizerPanel(String id) {
        super(id);
        addTreeTable();
    }

    private void addTreeTable() {
        visualizerTreeGrid = new VisualizerTreeGrid("treeTable", createEmptyTreeGridModel(), createEmptyTreeGridColumns());
        add(visualizerTreeGrid);
    }

    private List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> createEmptyTreeGridColumns() {
        List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> columns;
        columns = new ArrayList<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>>();
        columns.add(new VisualizerTreeColumn("tree", Model.of("Tree")));
        /*columns.add(new VisualizerPropertyColumn("P1", "userObject.p1", "userObject.p1"));
        columns.add(new VisualizerPropertyColumn("P2", "userObject.p2", "userObject.p2"));
        columns.add(new VisualizerPropertyColumn("P3", "userObject.p3", "userObject.p3"));*/
        return columns;
    }

    private VisualizerTreeModel createEmptyTreeGridModel() {
        DatasetRow datasetRow = new DatasetRow();
        VisualizerTreeNode rootNode = new VisualizerTreeNode(new VisualizerTreeNodeData(datasetRow));
        return new VisualizerTreeModel(rootNode);
    }

    public void setVisualizerTreeModel(VisualizerTreeModel visualizerTreeModel, List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> visualizerTreeGridColumns) {
        visualizerTreeGrid = new VisualizerTreeGrid("treeTable", visualizerTreeModel, visualizerTreeGridColumns);
        addOrReplace(visualizerTreeGrid);
    }

}
