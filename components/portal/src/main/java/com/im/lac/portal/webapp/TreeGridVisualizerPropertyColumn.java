package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.PropertyDescriptor;
import com.im.lac.portal.service.api.Row;
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
        Row row = rowModel.getObject().getUserObject();
        PropertyDescriptor propertyDescriptor = row.getDescriptor().findPropertyDescriptorById(propertyId);
        String property = row.getProperty(propertyDescriptor).toString();
        return new Label(componentId, property);
    }
}
