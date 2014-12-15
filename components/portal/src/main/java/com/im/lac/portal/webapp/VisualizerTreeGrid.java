package com.im.lac.portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.treegrid.TreeGrid;

import java.util.List;

public class VisualizerTreeGrid extends TreeGrid<VisualizerTreeModel, VisualizerTreeNode, String> {

    public VisualizerTreeGrid(String id, VisualizerTreeModel model, List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> columns) {
        super(id, model, columns);
    }
}
