package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
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
        TreeGridVisualizerNode node = model.getObject();
        TreeGridVisualizerNodeData nodeData = node.getUserObject();
        if (propertyId == 0) {
            return new TreeGridVisualizerStructurePanel(id, datasetDescriptor.getId(), nodeData.getId());
        } else {
            Object value = nodeData.getPropertyValue(propertyId);
            Label label = new Label(id, value.toString());
            return label;
        }
    }

    @Override
    protected Icon getIcon(IModel<TreeGridVisualizerNode> defaultMutableTreeNodeIModel) {
        return null;
    }


}
