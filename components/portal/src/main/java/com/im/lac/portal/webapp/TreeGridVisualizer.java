package com.im.lac.portal.webapp;

import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.treegrid.TreeGrid;

import java.util.List;

public class TreeGridVisualizer extends TreeGrid<TreeGridVisualizerModel, TreeGridVisualizerNode, String> {

    public TreeGridVisualizer(String id, TreeGridVisualizerModel model, List<IGridColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String>> columns) {
        super(id, model, columns);
    }
}
