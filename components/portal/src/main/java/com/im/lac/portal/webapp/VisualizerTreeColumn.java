package com.im.lac.portal.webapp;

import com.inmethod.grid.common.Icons;
import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public class VisualizerTreeColumn extends BaseTreeColumn<VisualizerTreeModel, VisualizerTreeNode, String> {

    public VisualizerTreeColumn(String columnId, IModel<String> headerModel) {
        super(columnId, headerModel);
    }

    @Override
    protected Component newNodeComponent(String id, IModel<VisualizerTreeNode> model) {
        VisualizerTreeNode node = model.getObject();
        if (!node.isLeaf()) {
            return new VisualizerStructurePanel(id);
        } else {
            return new Label(id, new PropertyModel(model, "userObject.description"));
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
