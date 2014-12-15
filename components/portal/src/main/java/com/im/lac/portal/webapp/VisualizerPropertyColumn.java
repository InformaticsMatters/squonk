package com.im.lac.portal.webapp;

import com.inmethod.grid.column.PropertyColumn;
import org.apache.wicket.model.Model;

public class VisualizerPropertyColumn extends PropertyColumn<VisualizerTreeModel, VisualizerTreeNode, String, String> {

    public VisualizerPropertyColumn(String headerModel, String propertyExpression, String sortProperty) {
        super(Model.of(headerModel), propertyExpression, sortProperty);
    }
}
