package com.im.lac.portal.webapp;

import com.inmethod.grid.common.Icons;
import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class StructureTreeColumn extends BaseTreeColumn<DefaultTreeModel, DefaultMutableTreeNode, String> {

    public StructureTreeColumn(String columnId, IModel<String> headerModel) {
        super(columnId, headerModel);
    }

    @Override
    protected Component newNodeComponent(String id, IModel<DefaultMutableTreeNode> model) {
        DefaultMutableTreeNode node = model.getObject();
        if (!node.isLeaf()) {
            return new StructurePanel(id, new RowData());
        } else {
            return new Label(id, new PropertyModel(model, "userObject.description"));
        }
    }

    @Override
    protected Icon getIcon(IModel<DefaultMutableTreeNode> defaultMutableTreeNodeIModel) {
        DefaultMutableTreeNode node = defaultMutableTreeNodeIModel.getObject();
        if (node.isLeaf()) {
            return Icons.ITEM;
        } else {
            return null;
        }
    }
}
