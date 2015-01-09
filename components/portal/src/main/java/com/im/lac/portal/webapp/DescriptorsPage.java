package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.PropertyDescriptor;
import com.im.lac.portal.service.api.RowDescriptor;
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

    public DescriptorsPage() {
        notifierProvider.createNotifier(this, "notifier");
        add(new MenuPanel("menuPanel"));
        addDescriptorsTreeTable();
    }

    private void addDescriptorsTreeTable() {
        List<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>> columns = new ArrayList<IGridColumn<DefaultTreeModel, DefaultMutableTreeNode, String>>();

        PropertyTreeColumn treeColumn = new PropertyTreeColumn(Model.of("Description"), "userObject.description");
        columns.add(treeColumn);

        DefaultTreeModel model = createTreeModel();
        TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String> treeGrid = new TreeGrid<DefaultTreeModel, DefaultMutableTreeNode, String>("grid", model, columns);
        treeGrid.getTree().setRootLess(true);

        add(treeGrid);
    }

    private DefaultTreeModel createTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

        DefaultMutableTreeNode node;
        DescriptorNodeData data;

        DefaultMutableTreeNode datasetNode = new DefaultMutableTreeNode();
        data = new DescriptorNodeData(new DatasetDescriptor() {

            @Override
            public Long getId() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Dataset 1";
            }

            @Override
            public List<RowDescriptor> listAllRowDescriptors() {
                return null;
            }

            @Override
            public RowDescriptor findRowDescriptorById(Long id) {
                return null;
            }
        });
        datasetNode.setUserObject(data);
        rootNode.add(datasetNode);

        DefaultMutableTreeNode rowNode1 = new DefaultMutableTreeNode();
        data = new DescriptorNodeData(new DatasetDescriptor() {

            @Override
            public Long getId() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Row 1";
            }

            @Override
            public List<RowDescriptor> listAllRowDescriptors() {
                return null;
            }

            @Override
            public RowDescriptor findRowDescriptorById(Long id) {
                return null;
            }
        });
        rowNode1.setUserObject(data);
        datasetNode.add(rowNode1);

        DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode();
        data = new DescriptorNodeData(new PropertyDescriptor() {

            @Override
            public Long getId() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Property 1";
            }
        });
        propertyNode.setUserObject(data);
        rowNode1.add(propertyNode);

        DefaultMutableTreeNode rowNode2 = new DefaultMutableTreeNode();
        data = new DescriptorNodeData(new RowDescriptor() {

            @Override
            public Long getId() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Row 2";
            }

            @Override
            public List<PropertyDescriptor> listAllPropertyDescriptors() {
                return null;
            }

            @Override
            public PropertyDescriptor findPropertyDescriptorById(Long id) {
                return null;
            }
        });
        rowNode2.setUserObject(data);
        datasetNode.add(rowNode2);

        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        return model;
    }

}
