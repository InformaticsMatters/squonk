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
import javax.swing.tree.TreeModel;
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
        List<IGridColumn> columns = new ArrayList<IGridColumn>();

        columns.add(new PropertyTreeColumn(new Model("Description"), "userObject.description"));

        TreeModel model = createTreeModel();
        TreeGrid grid = new TreeGrid("grid", model, columns);

        add(grid);
    }

    private TreeModel createTreeModel() {
        DescriptorNodeData dnd1 = new DescriptorNodeData();
        dnd1.setDescriptorType(DescriptorNodeData.DescriptorType.DATASET);
        dnd1.setDatasetDescriptor(new DatasetDescriptor() {
            @Override
            public Long getId() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Descriptor 1";
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

        DescriptorNodeData dnd11 = new DescriptorNodeData();
        dnd11.setDescriptorType(DescriptorNodeData.DescriptorType.ROW);
        dnd11.setDatasetDescriptor(new DatasetDescriptor() {
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
        dnd1.add(dnd11);

        DescriptorNodeData dnd12 = new DescriptorNodeData();
        dnd12.setDescriptorType(DescriptorNodeData.DescriptorType.ROW);
        dnd12.setRowDescriptor(new RowDescriptor() {
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
        dnd1.add(dnd12);

        DescriptorNodeData dnd121 = new DescriptorNodeData();
        dnd121.setDescriptorType(DescriptorNodeData.DescriptorType.PROPERTY);
        dnd121.setPropertyDescriptor(new PropertyDescriptor() {
            @Override
            public Long getId() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Property 1";
            }
        });
        dnd12.add(dnd121);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(dnd1, true);
        TreeModel model = new DefaultTreeModel(rootNode);
        return model;
    }

}
