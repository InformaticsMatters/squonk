package com.im.lac.portal.webapp;

import com.inmethod.grid.column.PropertyColumn;
import org.apache.wicket.model.Model;

public class TreeGridVisualizerPropertyColumn extends PropertyColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String, String> {

    public TreeGridVisualizerPropertyColumn(String headerModel, String propertyExpression, String sortProperty) {
        super(Model.of(headerModel), propertyExpression, sortProperty);
    }
}
