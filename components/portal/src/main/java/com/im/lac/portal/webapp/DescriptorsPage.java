package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.*;
import com.im.lac.wicket.semantic.NotifierProvider;
import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.column.tree.PropertyTreeColumn;
import com.inmethod.grid.treegrid.TreeGrid;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

import javax.inject.Inject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public class DescriptorsPage extends WebPage {

    @Inject
    private NotifierProvider notifierProvider;
    @Inject
    private DatasetService service;

    public DescriptorsPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addDescriptorsTreeTable();
    }

    private void addDescriptorsTreeTable() {
        List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> columns = new ArrayList<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>>();

        PropertyTreeColumn treeColumn = new PropertyTreeColumn(Model.of("Description"), "userObject.description");
        treeColumn.setInitialSize(200);
        columns.add(treeColumn);

        DefaultTreeModel model = createTreeModel();
        TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String> treeGrid = new TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String>("grid", model, columns);
        treeGrid.getTree().setRootLess(true);

        add(treeGrid);
    }

    private DefaultTreeModel createTreeModel() {
        List<DatasetDescriptor> datasetDescriptorList = service.listDatasetDescriptor(new ListDatasetDescriptorFilter());

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

        for (DatasetDescriptor dd : datasetDescriptorList) {
            createDatasetDescriptorNode(rootNode, dd);
        }

        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        return model;
    }

    private void createDatasetDescriptorNode(DefaultMutableTreeNode rootNode, DatasetDescriptor dd) {
        DefaultMutableTreeNode datasetNode = new DefaultMutableTreeNode();
        DescriptorNodeData data = new DescriptorNodeData(dd);
        datasetNode.setUserObject(data);
        rootNode.add(datasetNode);

        List<RowDescriptor> rowDescriptorList = dd.listAllRowDescriptors();
        for (RowDescriptor rowDescriptor : rowDescriptorList) {
            createRowDescriptorNode(datasetNode, rowDescriptor);
        }
    }

    private void createRowDescriptorNode(DefaultMutableTreeNode datasetNode, RowDescriptor rowDescriptor) {
        DefaultMutableTreeNode rowNode = new DefaultMutableTreeNode();
        DescriptorNodeData data = new DescriptorNodeData(rowDescriptor);
        rowNode.setUserObject(data);
        datasetNode.add(rowNode);

        List<PropertyDescriptor> propertyDescriptorList = rowDescriptor.listAllPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptorList) {
            createPropertuDescriptorNode(rowNode, propertyDescriptor);
        }
    }

    private void createPropertuDescriptorNode(DefaultMutableTreeNode rowNode, PropertyDescriptor propertyDescriptor) {
        DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode();
        DescriptorNodeData data = new DescriptorNodeData(propertyDescriptor);
        rowNode.setUserObject(data);
        rowNode.add(propertyNode);
    }

}
