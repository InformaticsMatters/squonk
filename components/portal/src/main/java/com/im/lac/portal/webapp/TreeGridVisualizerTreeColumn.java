package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.Row;
import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;


public class TreeGridVisualizerTreeColumn extends BaseTreeColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String> {

    private DatasetDescriptor datasetDescriptor;
    private Long propertyId;

    public TreeGridVisualizerTreeColumn(String columnId, IModel<String> headerModel, DatasetDescriptor datasetDescriptor, Long propertyId) {
        super(columnId, headerModel);
        this.datasetDescriptor = datasetDescriptor;
        this.propertyId = propertyId;
    }

    @Override
    protected Component newNodeComponent(String id, IModel<TreeGridVisualizerNode> model) {
        Row row = model.getObject().getUserObject();
        if (propertyId == 0) {
            return new TreeGridVisualizerStructurePanel(id, datasetDescriptor.getId(), row.getId());
        } else {
            Object value = row.getProperty(row.getDescriptor().findPropertyDescriptorById(propertyId));
            return new Label(id, value.toString());
        }
    }

    @Override
    protected Icon getIcon(IModel<TreeGridVisualizerNode> defaultMutableTreeNodeIModel) {
        return null;
    }


}
