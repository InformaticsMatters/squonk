package com.im.lac.portal.webapp;

import com.im.lac.portal.service.*;
import com.im.lac.wicket.semantic.NotifierProvider;
import com.inmethod.grid.IGridColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class TreeGridDatasetVisualizerPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private PrototypeService service;
    private VisualizerPanel visualizerPanel;

    public TreeGridDatasetVisualizerPage() {
        notifierProvider.createNotifier(this, "notifier");
        addVisualizerPanel();
    }

    private void addVisualizerPanel() {
        visualizerPanel = new VisualizerPanel("visualizerPanel");
        add(visualizerPanel);
    }

    public void refreshVisualizerPanel(DatasetDescriptor datasetDescriptor) {
        ListDatasetRowFilter listDatasetRowFilter = new ListDatasetRowFilter();
        listDatasetRowFilter.setDatasetid(datasetDescriptor.getDatasetId());

        List<DatasetRow> datasetRowList = service.listDatasetRow(listDatasetRowFilter);

        DatasetRow rootDatasetRow = new DatasetRow();
        VisualizerTreeNode rootNode = new VisualizerTreeNode(new VisualizerTreeNodeData(rootDatasetRow));
        //VisualizerTreeNode rootNode = new VisualizerTreeNode(new VisualizerTreeNodeData("root", "", ""));
        addDatasetChildren(rootNode, datasetRowList);
        List<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>> columns =  new ArrayList<IGridColumn<VisualizerTreeModel, VisualizerTreeNode, String>>();
        columns.add(new VisualizerTreeColumn("id", Model.of("Structure"), datasetDescriptor));
        visualizerPanel.setVisualizerTreeModel(new VisualizerTreeModel(rootNode), columns);
    }

    private void addDatasetChildren(VisualizerTreeNode rootNode, List<DatasetRow> datasetRowList) {
        for(DatasetRow dsr : datasetRowList) {
            VisualizerTreeNode childNode = new VisualizerTreeNode(new VisualizerTreeNodeData(dsr));
            rootNode.add(childNode);
            if (dsr.getChildren() != null && dsr.getChildren().size() > 0) {
                addDatasetChildren(childNode, dsr.getChildren());
            }
        }
    }
}
