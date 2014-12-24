package com.im.lac.portal.webapp;

import com.inmethod.grid.column.AbstractColumn;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

public class TreeGridVisualizerPropertyColumn extends AbstractColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String> {

    private Long propertyId;

    public TreeGridVisualizerPropertyColumn(String columnId, IModel<String> headerModel, Long propertyId) {
        super(columnId, headerModel);
        this.propertyId = propertyId;
    }

    @Override
    public Component newCell(WebMarkupContainer parent, String componentId, IModel<TreeGridVisualizerNode> rowModel) {
        String propertyValue = rowModel.getObject().getUserObject().getPropertyValue(propertyId).toString();
        return new Label(componentId, propertyValue);
    }
}
