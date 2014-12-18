package com.im.lac.portal.webapp;

import com.im.lac.portal.service.DatasetDescriptor;
import com.im.lac.portal.service.DatasetRow;
import com.im.lac.portal.service.ListDatasetRowFilter;
import com.im.lac.portal.service.PrototypeService;
import com.im.lac.wicket.semantic.NotifierProvider;
import com.im.lac.wicket.semantic.SemanticResourceReference;
import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.common.AbstractGrid;
import com.vaynberg.wicket.select2.ApplicationSettings;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

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
        addPanels();
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptHeaderItem.forReference(SemanticResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(AbstractGrid.class, "res/style.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(ApplicationSettings.class, "res/select2.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/semantic-overrides.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(SemanticResourceReference.class, "resources/easygrid-overrides.css")));
    }

    private void addPanels() {
        add(new MenuPanel("menuPanel"));

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
