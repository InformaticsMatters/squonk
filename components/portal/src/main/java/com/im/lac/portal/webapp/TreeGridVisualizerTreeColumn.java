package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.inmethod.grid.treegrid.BaseTreeColumn;
import com.inmethod.icon.Icon;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

public class TreeGridVisualizerTreeColumn extends BaseTreeColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String> {

    private DatasetDescriptor datasetDescriptor;

    public TreeGridVisualizerTreeColumn(String columnId, IModel<String> headerModel, DatasetDescriptor datasetDescriptor) {
        super(columnId, headerModel);
        this.datasetDescriptor = datasetDescriptor;
    }

    @Override
    protected Component newNodeComponent(String id, IModel<TreeGridVisualizerNode> model) {
        TreeGridVisualizerNode node = model.getObject();
        TreeGridVisualizerNodeData vtnd = node.getUserObject();
        if (vtnd.getId() != null) {
            return new TreeGridVisualizerStructurePanel(id, datasetDescriptor.getDatasetId(), vtnd.getId());
        } else {
            return new WebMarkupContainer(id);
        }
    }

    @Override
    protected Icon getIcon(IModel<TreeGridVisualizerNode> defaultMutableTreeNodeIModel) {
        return null;
    }


}
