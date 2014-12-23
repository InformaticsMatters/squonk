package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.DatasetRow;
import com.im.lac.portal.service.api.DatasetService;
import com.im.lac.portal.service.api.ListDatasetRowFilter;
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

public class TreeGridVisualizerPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;

    public TreeGridVisualizerPage(DatasetDescriptor datasetDescriptor) {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addTreeGrid(datasetDescriptor);
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

    public void addTreeGrid(DatasetDescriptor datasetDescriptor) {
        ListDatasetRowFilter listDatasetRowFilter = new ListDatasetRowFilter();
        listDatasetRowFilter.setDatasetId(datasetDescriptor.getDatasetId());
        List<DatasetRow> datasetRowList = service.listDatasetRow(listDatasetRowFilter);

        TreeGridVisualizerNode rootNode = new TreeGridVisualizerNode(new TreeGridVisualizerNodeData(new DatasetRow()));
        buildNodeHierarchy(rootNode, datasetRowList);

        List<IGridColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String>> columns = new ArrayList<IGridColumn<TreeGridVisualizerModel, TreeGridVisualizerNode, String>>();
        columns.add(new TreeGridVisualizerTreeColumn("id", Model.of("Structure"), datasetDescriptor));

        TreeGridVisualizer treeGridVisualizer = new TreeGridVisualizer("treeGrid", new TreeGridVisualizerModel(rootNode), columns);
        treeGridVisualizer.getTree().setRootLess(true);
        add(treeGridVisualizer);
    }

    private void buildNodeHierarchy(TreeGridVisualizerNode rootNode, List<DatasetRow> datasetRowList) {
        for (DatasetRow datasetRow : datasetRowList) {
            TreeGridVisualizerNode childNode = new TreeGridVisualizerNode(new TreeGridVisualizerNodeData(datasetRow));
            rootNode.add(childNode);
            if (datasetRow.getChildren() != null && datasetRow.getChildren().size() > 0) {
                buildNodeHierarchy(childNode, datasetRow.getChildren());
            }
        }
    }
}
