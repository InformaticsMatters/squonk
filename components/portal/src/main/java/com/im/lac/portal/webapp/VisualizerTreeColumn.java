package com.im.lac.portal.webapp;

import com.im.lac.portal.service.DatasetDescriptor;
import com.inmethod.grid.common.Icons;
import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public class VisualizerTreeColumn extends BaseTreeColumn<VisualizerTreeModel, VisualizerTreeNode, String> {

    private DatasetDescriptor datasetDescriptor;

    public VisualizerTreeColumn(String columnId, IModel<String> headerModel, DatasetDescriptor datasetDescriptor) {
        super(columnId, headerModel);
        this.datasetDescriptor = datasetDescriptor;
    }

    @Override
    protected Component newNodeComponent(String id, IModel<VisualizerTreeNode> model) {
        VisualizerTreeNode node = model.getObject();
        VisualizerTreeNodeData vtnd = node.getUserObject();
        if (vtnd.getId() != null) {
            return new VisualizerStructurePanel(id, vtnd.getId(), datasetDescriptor.getDatasetId());
        } else {
            return new Label(id, new PropertyModel(model, "userObject.dummy"));
        }
    }

    @Override
    protected Icon getIcon(IModel<VisualizerTreeNode> defaultMutableTreeNodeIModel) {
        VisualizerTreeNode node = defaultMutableTreeNodeIModel.getObject();
        if (node.isLeaf()) {
            return Icons.ITEM;
        } else {
            return null;
        }
    }
}
